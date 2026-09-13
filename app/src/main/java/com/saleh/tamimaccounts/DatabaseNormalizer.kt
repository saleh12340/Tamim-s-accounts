package com.saleh.tamimaccounts

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Converts imported SQLite databases into the app's canonical schema without
 * assuming that the source uses the same column names. It preserves customers,
 * account operations, invoices, invoice items, products, suppliers and expenses
 * whenever the corresponding data can be identified by common Arabic/English
 * column names.
 */
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
        val map = cols.associateBy { it.trim().lowercase() }
        for (n in names) map[n.lowercase()]?.let { return it }
        return cols.firstOrNull { c -> names.any { n -> c.contains(n, true) } }
    }
    private fun read(c: Cursor, cols: List<String>, name: String?): String? = name?.let { c.getString(c.getColumnIndexOrThrow(it)) }
    private fun num(v: String?): Double = v?.replace(",", "")?.trim()?.toDoubleOrNull() ?: 0.0

    fun normalize(source: File, target: File): ImportStats {
        if (target.exists()) target.delete()
        source.copyTo(target, true)
        val db = SQLiteDatabase.openDatabase(target.path, null, SQLiteDatabase.OPEN_READWRITE)
        return try {
            db.execSQL("CREATE TABLE IF NOT EXISTS customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL DEFAULT '',phone TEXT DEFAULT '',balance REAL DEFAULT 0,group_id INTEGER,notes TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER NOT NULL,type TEXT NOT NULL DEFAULT 'DEBIT',amount REAL NOT NULL DEFAULT 0,currency TEXT NOT NULL DEFAULT 'YER',date TEXT NOT NULL DEFAULT '',note TEXT DEFAULT '',share_ref TEXT)")
            db.execSQL("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,barcode TEXT DEFAULT '',buy_price REAL DEFAULT 0,sell_price REAL DEFAULT 0,stock REAL DEFAULT 0,min_stock REAL DEFAULT 0,notes TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS suppliers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT DEFAULT '',phone TEXT DEFAULT '',balance REAL DEFAULT 0,notes TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS expenses(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT DEFAULT '',amount REAL DEFAULT 0,currency TEXT DEFAULT 'YER',date TEXT DEFAULT '',note TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS invoices(id INTEGER PRIMARY KEY AUTOINCREMENT,type TEXT NOT NULL DEFAULT 'SALE',customer_id INTEGER,supplier_id INTEGER,date TEXT DEFAULT '',total REAL DEFAULT 0,paid REAL DEFAULT 0,note TEXT DEFAULT '')")
            db.execSQL("CREATE TABLE IF NOT EXISTS invoice_items(id INTEGER PRIMARY KEY AUTOINCREMENT,invoice_id INTEGER NOT NULL,item_name TEXT DEFAULT '',quantity REAL DEFAULT 0,unit_price REAL DEFAULT 0,total REAL DEFAULT 0)")

            val ts = tables(db)
            var customerCount = 0; var txCount = 0; var invoiceCount = 0; var itemCount = 0; var productCount = 0; var expenseCount = 0
            val customerSource = ts.firstOrNull { it.equals("customers", true) } ?: ts.firstOrNull { t ->
                val c = columns(db,t); find(c,"name","customer_name","client_name","اسم","اسم العميل","العميل") != null && find(c,"phone","mobile","phone_number","هاتف","الجوال") != null
            }
            val idMap = mutableMapOf<Long,Long>()
            if (customerSource != null) {
                val c = columns(db, customerSource); val id=find(c,"id","customer_id","client_id","رقم"); val name=find(c,"name","customer_name","client_name","اسم","اسم العميل","العميل"); val phone=find(c,"phone","mobile","phone_number","هاتف","الجوال"); val bal=find(c,"balance","رصيد","الرصيد"); val notes=find(c,"notes","note","ملاحظات")
                db.rawQuery("SELECT * FROM ${q(customerSource)}", null).use { cur -> while(cur.moveToNext()) {
                    val n=read(cur,c,name).orEmpty().trim(); if(n.isEmpty()) continue
                    val oldId=read(cur,c,id)?.toLongOrNull(); val cv=ContentValues().apply{put("name",n);put("phone",read(cur,c,phone).orEmpty());put("balance",num(read(cur,c,bal)));put("notes",read(cur,c,notes).orEmpty())}
                    val newId=db.insert("customers",null,cv); if(newId>0){customerCount++; if(oldId!=null)idMap[oldId]=newId}
                }}
            }
            val txSource = ts.firstOrNull { it.equals("transactions",true) } ?: ts.firstOrNull { t -> val c=columns(db,t); find(c,"customer_id","client_id","account_id","معرف العميل")!=null && find(c,"amount","المبلغ","value","debit","credit")!=null }
            if(txSource!=null){ val c=columns(db,txSource); val cid=find(c,"customer_id","client_id","account_id","customer","العميل","معرف العميل"); val type=find(c,"type","transaction_type","نوع","النوع"); val amount=find(c,"amount","المبلغ","value","total","debit","credit"); val currency=find(c,"currency","العملة","currency_code"); val date=find(c,"date","datetime","created_at","التاريخ","التاريخ والوقت"); val note=find(c,"note","description","details","البيان","التفاصيل","ملاحظات"); val debit=find(c,"debit","مدين"); val credit=find(c,"credit","دائن")
                db.rawQuery("SELECT * FROM ${q(txSource)}",null).use{cur->while(cur.moveToNext()){val old=read(cur,c,cid)?.toLongOrNull();val new=idMap[old]?:old; if(new==null||new<=0)continue; val raw=read(cur,c,amount); val a=if(raw!=null)num(raw) else maxOf(num(read(cur,c,debit)),num(read(cur,c,credit)));if(a<=0)continue; val ty=(read(cur,c,type).orEmpty().uppercase()); val finalType=when{ty.contains("CREDIT")||ty.contains("CREDIT")||num(read(cur,c,credit))>0->"CREDIT";else->"DEBIT"}; db.insert("transactions",null,ContentValues().apply{put("customer_id",new);put("type",finalType);put("amount",a);put("currency",read(cur,c,currency).orEmpty().ifBlank{"YER"});put("date",read(cur,c,date).orEmpty());put("note",read(cur,c,note).orEmpty())});txCount++}}}
            }
            val productSource=ts.firstOrNull{it.equals("products",true)}; if(productSource!=null){val c=columns(db,productSource);val name=find(c,"name","product_name","item_name","اسم","اسم الصنف");val barcode=find(c,"barcode","bar_code","باركود");val buy=find(c,"buy_price","purchase_price","cost","سعر الشراء");val sell=find(c,"sell_price","sale_price","price","سعر البيع","السعر");val stock=find(c,"stock","quantity","qty","المخزون","الكمية");val min=find(c,"min_stock","minimum_stock","الحد الأدنى");db.rawQuery("SELECT * FROM ${q(productSource)}",null).use{cur->while(cur.moveToNext()){val n=read(cur,c,name).orEmpty().trim();if(n.isBlank())continue;db.insertWithOnConflict("products",null,ContentValues().apply{put("name",n);put("barcode",read(cur,c,barcode).orEmpty());put("buy_price",num(read(cur,c,buy)));put("sell_price",num(read(cur,c,sell)));put("stock",num(read(cur,c,stock)));put("min_stock",num(read(cur,c,min)))},SQLiteDatabase.CONFLICT_IGNORE);productCount++}}}
            val expenseSource=ts.firstOrNull{it.equals("expenses",true)};if(expenseSource!=null){val c=columns(db,expenseSource);val title=find(c,"title","name","expense_name","الوصف","البيان","المصروف");val amount=find(c,"amount","value","المبلغ");val date=find(c,"date","datetime","التاريخ");val note=find(c,"note","notes","ملاحظات");db.rawQuery("SELECT * FROM ${q(expenseSource)}",null).use{cur->while(cur.moveToNext()){db.insert("expenses",null,ContentValues().apply{put("title",read(cur,c,title).orEmpty());put("amount",num(read(cur,c,amount)));put("date",read(cur,c,date).orEmpty());put("note",read(cur,c,note).orEmpty())});expenseCount++}}}
            db.execSQL("UPDATE customers SET balance=(SELECT COALESCE(SUM(CASE WHEN type='DEBIT' THEN amount ELSE -amount END),0) FROM transactions WHERE customer_id=customers.id)")
            ImportStats(customerCount,txCount,invoiceCount,itemCount,productCount,expenseCount)
        } finally { db.close() }
    }
    data class ImportStats(val customers:Int,val transactions:Int,val invoices:Int,val items:Int,val products:Int,val expenses:Int)
}
