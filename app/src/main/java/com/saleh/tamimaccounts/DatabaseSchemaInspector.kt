package com.saleh.tamimaccounts

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File

/** Read-only inspection of an imported SQLite database before it is accepted. */
object DatabaseSchemaInspector {
    data class TableInfo(val name: String, val columns: List<String>, val rowCount: Long)
    data class Report(
        val validSqlite: Boolean,
        val integrityOk: Boolean,
        val tables: List<TableInfo>,
        val warnings: List<String>
    )

    fun inspect(file: File): Report {
        if (!file.exists() || file.length() < 100) {
            return Report(false, false, emptyList(), listOf("الملف غير موجود أو غير صالح"))
        }
        return try {
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                val integrity = db.rawQuery("PRAGMA integrity_check", null).use { it.moveToFirst() && it.getString(0).equals("ok", true) }
                val tables = mutableListOf<TableInfo>()
                db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name", null).use { c ->
                    while (c.moveToNext()) {
                        val name = c.getString(0)
                        val columns = mutableListOf<String>()
                        db.rawQuery("PRAGMA table_info(\"${name.replace("\"", "\"\"")}\")", null).use { info ->
                            while (info.moveToNext()) columns += info.getString(info.getColumnIndexOrThrow("name"))
                        }
                        val count = try {
                            db.rawQuery("SELECT COUNT(*) FROM \"${name.replace("\"", "\"\"")}\"", null).use { n -> if (n.moveToFirst()) n.getLong(0) else 0L }
                        } catch (_: Exception) { 0L }
                        tables += TableInfo(name, columns, count)
                    }
                }
                val warnings = mutableListOf<String>()
                if (!tables.any { it.name == "customers" }) warnings += "جدول العملاء غير موجود"
                if (!tables.any { it.name == "transactions" }) warnings += "جدول العمليات غير موجود"
                Report(true, integrity, tables, warnings)
            }
        } catch (e: Exception) {
            Report(false, false, emptyList(), listOf("تعذر فتح قاعدة SQLite: ${e.message ?: "خطأ غير معروف"}"))
        }
    }
}
