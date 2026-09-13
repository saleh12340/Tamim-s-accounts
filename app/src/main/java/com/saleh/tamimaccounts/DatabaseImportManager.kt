package com.saleh.tamimaccounts

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import java.io.File

/** Safe, repeatable import of SQLite databases selected by the user. */
object DatabaseImportManager {
    data class Result(
        val ok: Boolean,
        val message: String,
        val tables: List<DatabaseSchemaInspector.TableInfo> = emptyList()
    )

    fun importDb(context: Context, uri: Uri): Result {
        val tmp = File(context.cacheDir, "database-import-${System.currentTimeMillis()}.db")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tmp.outputStream().use { output -> input.copyTo(output) }
            } ?: return Result(false, "تعذر قراءة الملف المحدد")

            if (!isSqlite(tmp)) return Result(false, "الملف المحدد ليس قاعدة SQLite صالحة")

            val report = DatabaseSchemaInspector.inspect(tmp)
            if (!report.validSqlite || !report.integrityOk) {
                return Result(false, "فشل فحص سلامة قاعدة البيانات: ${report.warnings.joinToString("، ")}", report.tables)
            }

            val hasCustomers = report.tables.any { it.name == "customers" }
            val hasTransactions = report.tables.any { it.name == "transactions" }
            val hasInvoices = report.tables.any { it.name == "invoices" }
            if (!hasCustomers) return Result(false, "قاعدة البيانات لا تحتوي على جدول العملاء", report.tables)
            if (!hasTransactions && !hasInvoices) {
                return Result(false, "لم يتم العثور على جدول العمليات أو الفواتير", report.tables)
            }

            val target = context.getDatabasePath(DatabaseHelper.DB_NAME)
            val backup = File(target.path + ".pre-import-backup")
            val helper = DatabaseHelper(context)
            try {
                helper.writableDatabase.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null).use { }
            } catch (_: Exception) {
                // A database without WAL can safely continue.
            } finally {
                helper.close()
            }

            if (target.exists()) target.copyTo(backup, true)
            try {
                tmp.copyTo(target, true)
                File(target.path + "-wal").delete()
                File(target.path + "-shm").delete()
                File(target.path + "-journal").delete()

                val finalReport = DatabaseSchemaInspector.inspect(target)
                if (!finalReport.validSqlite || !finalReport.integrityOk) {
                    throw IllegalStateException("فشل التحقق من النسخة المستوردة")
                }

                backup.delete()
                Result(true, "تم استيراد قاعدة البيانات بنجاح (${finalReport.tables.size} جداول)", finalReport.tables)
            } catch (e: Exception) {
                if (backup.exists()) backup.copyTo(target, true)
                Result(false, "فشل الاستيراد وتمت المحافظة على قاعدة البيانات السابقة: ${e.message ?: "خطأ غير معروف"}", report.tables)
            }
        } catch (e: Exception) {
            Result(false, "فشل الاستيراد: ${e.message ?: "خطأ غير معروف"}")
        } finally {
            tmp.delete()
        }
    }

    private fun isSqlite(file: File): Boolean = try {
        file.inputStream().use { input ->
            val header = ByteArray(16)
            input.read(header) == 16 && String(header, 0, 6, Charsets.US_ASCII) == "SQLite"
        }
    } catch (_: Exception) {
        false
    }
}
