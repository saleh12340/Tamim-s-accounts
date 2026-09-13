package com.saleh.tamimaccounts

import android.content.Context

/**
 * Keeps the existing ModernActivity calls (`AlertDialog.Builder(...)`)
 * source-compatible without relying on a Kotlin typealias for a Java nested class.
 */
class AlertDialog {
    class Builder(context: Context) : android.app.AlertDialog.Builder(context)
}
