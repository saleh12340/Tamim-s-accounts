package com.saleh.tamimaccounts

import android.content.Context
import android.net.Uri

/** Compatibility helpers for the current ModernActivity source. */
typealias AlertDialog = android.app.AlertDialog

fun DatabaseHelper.integrityCheck(): Boolean = try {
    integrityCheck(databasePath())
} catch (_: Exception) {
    false
}

private fun DatabaseHelper.databasePath(): String {
    val field = DatabaseHelper::class.java.getDeclaredField("ctx")
    field.isAccessible = true
    val context = field.get(this) as Context
    return context.getDatabasePath(DatabaseHelper.DB_NAME).path
}

fun DatabaseImportManager.import(context: Context, uri: Uri): DatabaseImportManager.Result =
    DatabaseImportManager.importDb(context, uri)
