package com.saleh.tamimaccounts

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File

/** Safe repeatable SQLite importer. Canonical databases are preserved as-is;
 * non-canonical sources are mapped using common Arabic/English column names. */
object DatabaseNormalizer {
    private fun q(s: String) = "`" + s.replace("`", "``") + "`"
    private fun tables(db: SQLiteDatabase): List<String> = buildList {
        db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'", null).use { c ->
            while (c.moveToNext()) add(c.getString(0))
        }
    }
    private fun columns(db: SQLiteDatabase, table: String): List<String> = buildList {
        db.rawQuery("PRAGMA table_info(${q(table)})", null).use { c -> while (c.moveToNext()) add(c.getString(1)) }
    }
    private fun find(cols: List<String>, vararg names: String): String? {
        val exact = cols.associateBy { it.trim().lowercase() }
        names.forEach { n -> exact[n.lowercase()]?.let { return it } }
        return cols.firstOrNull { c -> names.any { n -> c.contains(n, ignoreCase = true) } }
    }
    private fun read(c: Cursor, name: String?): String? = name?.let { c.getString(c.getColumnIndex(it)) }
    private fun num(v: String?): Double = v?.replace(",", "")?.trim()?.toDoubleOrNull() ?: 0.0
    private fun count(db: SQLiteDatabase, table: String): Int = try {
        db.rawQuery("SELECT COUNT(*) FROM ${q(table)}", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    } catch (_: Exception) { 0 }

    fun normalize(source: File, target: File): ImportStats {
        if (target.exists()) target.delete()
        source.copyTo(target, true)
        val db = SQLiteDatabase.openDatabase(target.path, null, SQLiteDatabase.OPEN_READWRITE)
        return try {
            val existing = tables(db)
            val canonical = existing.any { it.equals("customers", true) } &&
                existing.any { it.equals("transactions", true) }
            if (canonical) {
                return ImportStats(
                    count(db, "customers"), count(db, "transactions"),
                    count(db, "invoices"), count(db, "invoice_items"),
                    count(db, "products"), count(db, "expenses")
                )
            }

            db.execSQL("CREATE TABLE IF NOT EXISTS customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL DEFAULT '',phone TEXT DEFAULT '',balance REAL DEFAULT 0,group_id INTEGER,notes TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER NOT NULL,type TEXT NOT NULL DEFAULT 'DEBIT',amount REAL NOT NULL DEFAULT 0,currency TEXT NOT NULL DEFAULT 'YER',date TEXT NOT NULL DEFAULT '',note TEXT DEFAULT '',share_ref TEXT)")
            db.execSQL("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,barcode TEXT DEFAULT '',buy_price REAL DEFAULT 0,sell_price REAL DEFAULT 0,stock REAL DEFAULT 0,min_stock REAL DEFAULT 0,notes TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS expenses(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT DEFAULT '',amount REAL DEFAULT 0,currency TEXT DEFAULT 'YER',date TEXT DEFAULT '',note TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS suppliers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT DEFAULT '',phone TEXT DEFAULT '',balance REAL DEFAULT 0,notes TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS invoices(id INTEGER PRIMARY KEY AUTOINCREMENT,type TEXT NOT NULL DEFAULT 'SALE',customer_id INTEGER,supplier_id INTEGER,date TEXT DEFAULT '',total REAL DEFAULT 0,paid REAL DEFAULT 0,note TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS invoice_items(id INTEGER PRIMARY KEY AUTOINCREMENT,invoice_id INTEGER NOT NULL,item_name TEXT DEFAULT '',quantity REAL DEFAULT 0,unit_price REAL DEFAULT 0,total REAL DEFAULT 0)")

            val ts = tables(db)
            var customers = 0; var transactions = 0; var products = 0; var expenses = 0
            val idMap = mutableMapOf<Long, Long>()
            val customerSource = ts.firstOrNull { t ->
                val c = columns(db, t)
                find(c, "name", "customer_name", "client_name", "اسم", "اسم العميل", "العميل") != null
            }
            if (customerSource != null && !customerSource.equals("customers", true)) {
                val c = columns(db, customerSource)
                val id = find(c, "id", "customer_id", "client_id", "رقم")
                val name = find(c, "name", "customer_name", "client_name", "اسم", "اسم العميل", "العميل")
                val phone = find(c, "phone", "mobile", "phone_number", "هاتف", "الجوال")
                val balance = find(c, "balance", "رصيد", "الرصيد")
                val notes = find(c, "notes", "note", "ملاحظات")
                db.rawQuery("SELECT * FROM ${q(customerSource)}", null).use { cur ->
                    while (cur.moveToNext()) {
                        val n = read(cur, name).orEmpty().trim()
                        if (n.isEmpty()) continue
                        val cv = ContentValues().apply {
                            put("name", n); put("phone", read(cur, phone).orEmpty())
                            put("balance", num(read(cur, balance))); put("notes", read(cur, notes).orEmpty())
                        }
                        val newId = db.insert("customers", null, cv)
                        val oldId = read(cur, id)?.toLongOrNull()
                        if (newId > 0) { customers++; if (oldId != null) idMap[oldId] = newId }
                    }
                }
            }

            val txSource = ts.firstOrNull { t ->
                val c = columns(db, t)
                find(c, "customer_id", "client_id", "account_id", "معرف العميل") != null &&
                    find(c, "amount", "المبلغ", "value", "debit", "credit") != null
            }
            if (txSource != null && !txSource.equals("transactions", true)) {
                val c = columns(db, txSource)
                val cid = find(c, "customer_id", "client_id", "account_id", "customer", "العميل", "معرف العميل")
                val type = find(c, "type", "transaction_type", "نوع", "النوع")
                val amount = find(c, "amount", "المبلغ", "value", "total")
                val debit = find(c, "debit", "مدين")
                val credit = find(c, "credit", "دائن")
                val currency = find(c, "currency", "العملة", "currency_code")
                val date = find(c, "date", "datetime", "created_at", "التاريخ", "التاريخ والوقت")
                val note = find(c, "note", "description", "details", "البيان", "التفاصيل", "ملاحظات")
                db.rawQuery("SELECT * FROM ${q(txSource)}", null).use { cur ->
                    while (cur.moveToNext()) {
                        val old = read(cur, cid)?.toLongOrNull() ?: continue
                        val newId = idMap[old] ?: old
                        val a = num(read(cur, amount)).takeIf { it > 0 } ?: maxOf(num(read(cur, debit)), num(read(cur, credit)))
                        if (newId <= 0 || a <= 0) continue
                        val rawType = read(cur, type).orEmpty().uppercase()
                        val finalType = if (rawType.contains("CREDIT") || rawType.contains("دائن") || num(read(cur, credit)) > 0) "CREDIT" else "DEBIT"
                        db.insert("transactions", null, ContentValues().apply {
                            put("customer_id", newId); put("type", finalType); put("amount", a)
                            put("currency", read(cur, currency).orEmpty().ifBlank { "YER" })
                            put("date", read(cur, date).orEmpty()); put("note", read(cur, note).orEmpty())
                        })
                        transactions++
                    }
                }
            }

            val productSource = ts.firstOrNull { it.equals("products", true) }
            if (productSource != null && !productSource.equals("products", true)) {
                val c = columns(db, productSource)
                val name = find(c, "name", "product_name", "item_name", "اسم", "اسم الصنف")
                val barcode = find(c, "barcode", "bar_code", "باركود")
                val buy = find(c, "buy_price", "purchase_price", "cost", "سعر الشراء")
                val sell = find(c, "sell_price", "sale_price", "price", "سعر البيع", "السعر")
                val stock = find(c, "stock", "quantity", "qty", "المخزون", "الكمية")
                val min = find(c, "min_stock", "minimum_stock", "الحد الأدنى")
                db.rawQuery("SELECT * FROM ${q(productSource)}", null).use { cur ->
                    while (cur.moveToNext()) {
                        val n = read(cur, name).orEmpty().trim(); if (n.isBlank()) continue
                        db.insertWithOnConflict("products", null, ContentValues().apply {
                            put("name", n); put("barcode", read(cur, barcode).orEmpty()); put("buy_price", num(read(cur, buy)))
                            put("sell_price", num(read(cur, sell))); put("stock", num(read(cur, stock))); put("min_stock", num(read(cur, min)))
                        }, SQLiteDatabase.CONFLICT_IGNORE)
                        products++
                    }
                }
            }

            val expenseSource = ts.firstOrNull { it.equals("expenses", true) }
            if (expenseSource != null && !expenseSource.equals("expenses", true)) {
                val c = columns(db, expenseSource)
                val title = find(c, "title", "name", "expense_name", "الوصف", "البيان", "المصروف")
                val amount = find(c, "amount", "value", "المبلغ")
                val date = find(c, "date", "datetime", "التاريخ")
                val note = find(c, "note", "notes", "ملاحظات")
                db.rawQuery("SELECT * FROM ${q(expenseSource)}", null).use { cur ->
                    while (cur.moveToNext()) {
                        db.insert("expenses", null, ContentValues().apply {
                            put("title", read(cur, title).orEmpty()); put("amount", num(read(cur, amount)))
                            put("date", read(cur, date).orEmpty()); put("note", read(cur, note).orEmpty())
                        }); expenses++
                    }
                }
            }
            db.execSQL("UPDATE customers SET balance=(SELECT COALESCE(SUM(CASE WHEN type='DEBIT' THEN amount ELSE -amount END),0) FROM transactions WHERE customer_id=customers.id)")
            ImportStats(customers, transactions, count(db, "invoices"), count(db, "invoice_items"), products, expenses)
        } finally { db.close() }
    }

    data class ImportStats(val customers: Int, val transactions: Int, val invoices: Int, val items: Int, val products: Int, val expenses: Int)
}
