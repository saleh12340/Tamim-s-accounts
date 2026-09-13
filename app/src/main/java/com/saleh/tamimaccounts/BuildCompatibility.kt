package com.saleh.tamimaccounts

import android.content.Context
import android.net.Uri

/** Compatibility helpers for the current ModernActivity source. */
fun DatabaseHelper.integrityCheck(): Boolean = try {
    val field = DatabaseHelper::class.java.getDeclaredField("ctx")
    field.isAccessible = true
    val context = field.get(this) as Context
    integrityCheck(context.getDatabasePath(DatabaseHelper.DB_NAME).path)
} catch (_: Exception) {
    false
}

fun DatabaseImportManager.import(context: Context, uri: Uri): DatabaseImportManager.Result =
    DatabaseImportManager.importDb(context, uri)

/** Allows the existing check-db screen to display its Boolean result as Arabic text. */
fun ModernActivity.toast(value: Boolean) {
    toast(if (value) "قاعدة البيانات سليمة" else "فشل فحص سلامة قاعدة البيانات")
}
