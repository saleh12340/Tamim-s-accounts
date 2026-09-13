package com.saleh.tamimaccounts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(private val ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {
    companion object { const val DB_NAME="app_database.db"; const val DB_VERSION=2 }
    override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT,balance REAL DEFAULT 0,group_id INTEGER,notes TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS groups(id INTEGER PRIMARY KEY AUTOINCREMENT,group_name TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS currency(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,symbol TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER NOT NULL,type TEXT NOT NULL,amount REAL NOT NULL,currency TEXT DEFAULT 'YER',date TEXT NOT NULL,note TEXT,share_ref TEXT,FOREIGN KEY(customer_id) REFERENCES customers(id) ON DELETE CASCADE)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS transactions_share_ref_uq ON transactions(share_ref)")
        db.execSQL("CREATE TABLE IF NOT EXISTS transactions_d(id INTEGER PRIMARY KEY AUTOINCREMENT,transaction_id INTEGER NOT NULL,item_name TEXT,quantity REAL,unit_price REAL,total_price REAL,FOREIGN KEY(transaction_id) REFERENCES transactions(id) ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE IF NOT EXISTS reminders(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER,reminder_date TEXT NOT NULL,note TEXT,is_completed INTEGER DEFAULT 0)")
        db.execSQL("CREATE VIEW IF NOT EXISTS transactions_tot_v AS SELECT c.id customer_id,c.name,SUM(CASE WHEN t.type='DEBIT' THEN t.amount ELSE 0 END) total_lah,SUM(CASE WHEN t.type='CREDIT' THEN t.amount ELSE 0 END) total_alayh,SUM(CASE WHEN t.type='DEBIT' THEN t.amount ELSE -t.amount END) total_amount FROM customers c LEFT JOIN transactions t ON t.customer_id=c.id GROUP BY c.id,c.name")
        if(db.rawQuery("SELECT COUNT(*) FROM currency",null).use{it.moveToFirst();it.getInt(0)}==0) db.execSQL("INSERT INTO currency(name,symbol) VALUES('ريال يمني','YER'),('ريال سعودي','SAR'),('دولار أمريكي','USD')")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion:Int, newVersion:Int){ onCreate(db) }
    fun customers():List<Customer>{ val a=mutableListOf<Customer>(); readableDatabase.rawQuery("SELECT id,name,COALESCE(phone,''),COALESCE(balance,0),COALESCE(notes,'') FROM customers ORDER BY name COLLATE NOCASE",null).use{c->while(c.moveToNext())a.add(Customer(c.getLong(0),c.getString(1),c.getString(2),c.getDouble(3),c.getString(4)))}; return a }
    fun customer(id:Long):Customer?=customers().firstOrNull{it.id==id}
    fun addCustomer(name:String,phone:String="",notes:String=""):Long{val v=ContentValues().apply{put("name",name.trim());put("phone",phone.trim());put("notes",notes)};return writableDatabase.insertOrThrow("customers",null,v)}
    fun updateCustomer(id:Long,name:String,phone:String,notes:String){val v=ContentValues().apply{put("name",name.trim());put("phone",phone.trim());put("notes",notes)};writableDatabase.update("customers",v,"id=?",arrayOf(id.toString()))}
    fun deleteCustomer(id:Long){writableDatabase.delete("customers","id=?",arrayOf(id.toString()))}
    fun addTransaction(customerId:Long,type:String,amount:Double,note:String,currency:String="YER",date:String=now()):Long{require(type=="DEBIT"||type=="CREDIT");require(amount>0);val v=ContentValues().apply{put("customer_id",customerId);put("type",type);put("amount",amount);put("currency",currency);put("date",date);put("note",note)};val id=writableDatabase.insertOrThrow("transactions",null,v);recalculate(customerId);return id}
    fun deleteTransaction(id:Long){val cid=writableDatabase.rawQuery("SELECT customer_id FROM transactions WHERE id=?",arrayOf(id.toString())).use{if(it.moveToFirst())it.getLong(0) else -1};writableDatabase.delete("transactions","id=?",arrayOf(id.toString()));if(cid>=0)recalculate(cid)}
    fun transactions(customerId:Long):List<Tx>{val a=mutableListOf<Tx>();readableDatabase.rawQuery("SELECT id,type,amount,currency,date,COALESCE(note,'') FROM transactions WHERE customer_id=? ORDER BY date,id",arrayOf(customerId.toString())).use{c->while(c.moveToNext())a.add(Tx(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4),c.getString(5)))};return a}
    private fun recalculate(cid:Long){writableDatabase.execSQL("UPDATE customers SET balance=(SELECT COALESCE(SUM(CASE WHEN type='DEBIT' THEN amount ELSE -amount END),0) FROM transactions WHERE customer_id=?) WHERE id=?",arrayOf(cid,cid))}
    fun integrityCheck(path:String):Boolean=try{SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY).use{d->d.rawQuery("PRAGMA integrity_check",null).use{it.moveToFirst()&&it.getString(0).equals("ok",true)}}}catch(_:Exception){false}
    private fun now()=SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(Date())
}
data class Customer(val id:Long,val name:String,val phone:String,val balance:Double,val notes:String)
data class Tx(val id:Long,val type:String,val amount:Double,val currency:String,val date:String,val note:String)
