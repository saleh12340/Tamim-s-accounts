package com.saleh.tamimaccounts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(private val ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {
    companion object { const val DB_NAME = "app_database.db"; const val DB_VERSION = 3 }
    override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
    override fun onCreate(db: SQLiteDatabase) { schema(db); seed(db) }
    private fun schema(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT DEFAULT '',balance REAL DEFAULT 0,group_id INTEGER,notes TEXT DEFAULT '')")
        db.execSQL("CREATE TABLE IF NOT EXISTS groups(id INTEGER PRIMARY KEY AUTOINCREMENT,group_name TEXT NOT NULL UNIQUE)")
        db.execSQL("CREATE TABLE IF NOT EXISTS currency(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,symbol TEXT NOT NULL UNIQUE)")
        db.execSQL("CREATE TABLE IF NOT EXISTS transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER NOT NULL,type TEXT NOT NULL CHECK(type IN ('DEBIT','CREDIT')),amount REAL NOT NULL CHECK(amount>0),currency TEXT NOT NULL DEFAULT 'YER',date TEXT NOT NULL,note TEXT DEFAULT '',share_ref TEXT,FOREIGN KEY(customer_id) REFERENCES customers(id) ON DELETE CASCADE)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS transactions_share_ref_uq ON transactions(share_ref) WHERE share_ref IS NOT NULL")
        db.execSQL("CREATE TABLE IF NOT EXISTS transactions_d(id INTEGER PRIMARY KEY AUTOINCREMENT,transaction_id INTEGER NOT NULL,item_name TEXT,quantity REAL,unit_price REAL,total_price REAL,FOREIGN KEY(transaction_id) REFERENCES transactions(id) ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE IF NOT EXISTS reminders(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER,reminder_date TEXT NOT NULL,note TEXT DEFAULT '',is_completed INTEGER DEFAULT 0,FOREIGN KEY(customer_id) REFERENCES customers(id) ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE,barcode TEXT DEFAULT '',buy_price REAL DEFAULT 0,sell_price REAL DEFAULT 0,stock REAL DEFAULT 0,min_stock REAL DEFAULT 0,notes TEXT DEFAULT '')")
        db.execSQL("CREATE TABLE IF NOT EXISTS suppliers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT DEFAULT '',balance REAL DEFAULT 0,notes TEXT DEFAULT '')")
        db.execSQL("CREATE TABLE IF NOT EXISTS expenses(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,amount REAL NOT NULL,currency TEXT DEFAULT 'YER',date TEXT NOT NULL,note TEXT DEFAULT '')")
        db.execSQL("CREATE TABLE IF NOT EXISTS invoices(id INTEGER PRIMARY KEY AUTOINCREMENT,type TEXT NOT NULL CHECK(type IN ('SALE','PURCHASE')),customer_id INTEGER,supplier_id INTEGER,date TEXT NOT NULL,total REAL DEFAULT 0,paid REAL DEFAULT 0,note TEXT DEFAULT '',FOREIGN KEY(customer_id) REFERENCES customers(id) ON DELETE SET NULL,FOREIGN KEY(supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS invoice_items(id INTEGER PRIMARY KEY AUTOINCREMENT,invoice_id INTEGER NOT NULL,item_name TEXT NOT NULL,quantity REAL NOT NULL,unit_price REAL NOT NULL,total REAL NOT NULL,FOREIGN KEY(invoice_id) REFERENCES invoices(id) ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE IF NOT EXISTS settings(key TEXT PRIMARY KEY,value TEXT NOT NULL)")
        db.execSQL("CREATE VIEW IF NOT EXISTS transactions_tot_v AS SELECT c.id customer_id,c.name,SUM(CASE WHEN t.type='DEBIT' THEN t.amount ELSE 0 END) total_lah,SUM(CASE WHEN t.type='CREDIT' THEN t.amount ELSE 0 END) total_alayh,SUM(CASE WHEN t.type='DEBIT' THEN t.amount ELSE -t.amount END) total_amount FROM customers c LEFT JOIN transactions t ON t.customer_id=c.id GROUP BY c.id,c.name")
    }
    private fun seed(db: SQLiteDatabase) {
        if (db.rawQuery("SELECT COUNT(*) FROM currency", null).use { it.moveToFirst(); it.getInt(0) } == 0)
            db.execSQL("INSERT INTO currency(name,symbol) VALUES('ريال يمني','YER'),('ريال سعودي','SAR'),('دولار أمريكي','USD')")
        db.execSQL("INSERT OR IGNORE INTO settings(key,value) VALUES('store_name','بقالة العزي للمواد الغذائية')")
        db.execSQL("INSERT OR IGNORE INTO settings(key,value) VALUES('phone','776425052')")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { schema(db); seed(db); recalculateAll(db) }
    private fun recalculateAll(db: SQLiteDatabase) { db.execSQL("UPDATE customers SET balance=(SELECT COALESCE(SUM(CASE WHEN type='DEBIT' THEN amount ELSE -amount END),0) FROM transactions WHERE customer_id=customers.id)") }

    fun customers(): List<Customer> { val a=mutableListOf<Customer>(); readableDatabase.rawQuery("SELECT id,name,COALESCE(phone,''),COALESCE(balance,0),COALESCE(notes,'') FROM customers ORDER BY name COLLATE NOCASE",null).use{c->while(c.moveToNext())a.add(Customer(c.getLong(0),c.getString(1),c.getString(2),c.getDouble(3),c.getString(4)))}; return a }
    fun customer(id:Long):Customer?=customers().firstOrNull{it.id==id}
    fun addCustomer(name:String,phone:String="",notes:String=""):Long { require(name.trim().isNotEmpty()); return writableDatabase.insertOrThrow("customers",null,ContentValues().apply{put("name",name.trim());put("phone",phone.trim());put("notes",notes)}) }
    fun updateCustomer(id:Long,name:String,phone:String,notes:String){writableDatabase.update("customers",ContentValues().apply{put("name",name.trim());put("phone",phone.trim());put("notes",notes)},"id=?",arrayOf(id.toString()))}
    fun deleteCustomer(id:Long){writableDatabase.delete("customers","id=?",arrayOf(id.toString()))}
    fun addTransaction(customerId:Long,type:String,amount:Double,note:String,currency:String="YER",date:String=now()):Long { require(type=="DEBIT"||type=="CREDIT"); require(amount>0); val id=writableDatabase.insertOrThrow("transactions",null,ContentValues().apply{put("customer_id",customerId);put("type",type);put("amount",amount);put("currency",currency);put("date",date);put("note",note)}); recalculate(customerId); return id }
    fun updateTransaction(id:Long,type:String,amount:Double,note:String,currency:String){val cid=transactionCustomer(id);writableDatabase.update("transactions",ContentValues().apply{put("type",type);put("amount",amount);put("note",note);put("currency",currency)},"id=?",arrayOf(id.toString()));if(cid>0)recalculate(cid)}
    fun deleteTransaction(id:Long){val cid=transactionCustomer(id);writableDatabase.delete("transactions","id=?",arrayOf(id.toString()));if(cid>0)recalculate(cid)}
    private fun transactionCustomer(id:Long)=writableDatabase.rawQuery("SELECT customer_id FROM transactions WHERE id=?",arrayOf(id.toString())).use{if(it.moveToFirst())it.getLong(0) else -1}
    fun transactions(customerId:Long):List<Tx>{val a=mutableListOf<Tx>();readableDatabase.rawQuery("SELECT id,type,amount,currency,date,COALESCE(note,'') FROM transactions WHERE customer_id=? ORDER BY date,id",arrayOf(customerId.toString())).use{c->while(c.moveToNext())a.add(Tx(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4),c.getString(5)))};return a}
    private fun recalculate(cid:Long){writableDatabase.execSQL("UPDATE customers SET balance=(SELECT COALESCE(SUM(CASE WHEN type='DEBIT' THEN amount ELSE -amount END),0) FROM transactions WHERE customer_id=?) WHERE id=?",arrayOf(cid,cid))}
    fun products():List<Product>{val a=mutableListOf<Product>();readableDatabase.rawQuery("SELECT id,name,barcode,buy_price,sell_price,stock,min_stock,notes FROM products ORDER BY name",null).use{c->while(c.moveToNext())a.add(Product(c.getLong(0),c.getString(1),c.getString(2),c.getDouble(3),c.getDouble(4),c.getDouble(5),c.getDouble(6),c.getString(7)))};return a}
    fun addProduct(name:String,barcode:String,buy:Double,sell:Double,stock:Double,min:Double,notes:String):Long= writableDatabase.insertOrThrow("products",null,ContentValues().apply{put("name",name.trim());put("barcode",barcode);put("buy_price",buy);put("sell_price",sell);put("stock",stock);put("min_stock",min);put("notes",notes)})
    fun deleteProduct(id:Long){writableDatabase.delete("products","id=?",arrayOf(id.toString()))}
    fun addExpense(title:String,amount:Double,note:String,currency:String="YER",date:String=now()):Long= writableDatabase.insertOrThrow("expenses",null,ContentValues().apply{put("title",title);put("amount",amount);put("note",note);put("currency",currency);put("date",date)})
    fun expenses():List<Expense>{val a=mutableListOf<Expense>();readableDatabase.rawQuery("SELECT id,title,amount,currency,date,note FROM expenses ORDER BY date DESC,id DESC",null).use{c->while(c.moveToNext())a.add(Expense(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4),c.getString(5)))};return a}
    fun deleteExpense(id:Long){writableDatabase.delete("expenses","id=?",arrayOf(id.toString()))}
    fun setting(key:String,default:String="")=readableDatabase.rawQuery("SELECT value FROM settings WHERE key=?",arrayOf(key)).use{if(it.moveToFirst())it.getString(0) else default}
    fun setSetting(key:String,value:String){writableDatabase.insertWithOnConflict("settings",null,ContentValues().apply{put("key",key);put("value",value)},SQLiteDatabase.CONFLICT_REPLACE)}
    fun integrityCheck(path:String):Boolean=try{SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY).use{d->d.rawQuery("PRAGMA integrity_check",null).use{it.moveToFirst()&&it.getString(0).equals("ok",true)}}}catch(_:Exception){false}
    fun now()=SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(Date())
}
data class Customer(val id:Long,val name:String,val phone:String,val balance:Double,val notes:String)
data class Tx(val id:Long,val type:String,val amount:Double,val currency:String,val date:String,val note:String)
data class Product(val id:Long,val name:String,val barcode:String,val buyPrice:Double,val sellPrice:Double,val stock:Double,val minStock:Double,val notes:String)
data class Expense(val id:Long,val title:String,val amount:Double,val currency:String,val date:String,val note:String)
