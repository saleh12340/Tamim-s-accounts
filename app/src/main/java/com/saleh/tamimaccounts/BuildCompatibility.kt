package com.saleh.tamimaccounts

import android.net.Uri

/** Compatibility helpers for the current ModernActivity source. */
typealias AlertDialog = android.app.AlertDialog

fun DatabaseHelper.integrityCheck(): Boolean = try {
    integrityCheck(contextDatabasePath())
} catch (_: Exception) {
    false
}

private fun DatabaseHelper.contextDatabasePath(): String =
    java.io.File(android.content.ContextWrapper(thisContext()).getDatabasePath(DatabaseHelper.DB_NAME).path).path

private fun DatabaseHelper.thisContext(): android.content.Context =
    try {
        val field = DatabaseHelper::class.java.getDeclaredField("ctx")
        field.isAccessible = true
        field.get(this) as android.content.Context
    } catch (_: Exception) {
        throw IllegalStateException("تعذر الوصول إلى مسار قاعدة البيانات")
    }

fun DatabaseImportManager.CompanionMarker() {}

fun DatabaseImportManager.import(context: android.content.Context, uri: Uri): DatabaseImportManager.Result =
    DatabaseImportManager.importDb(context, uri)
