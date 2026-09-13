package com.saleh.tamimaccounts

import android.content.*
import android.database.sqlite.*

class DatabaseHelper(ctx: Context) : SQLiteOpenHelper(ctx, "app_database.db", null, 1) {
    override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT,balance REAL DEFAULT 0,group_id INTEGER,notes TEXT)")
        db.execSQL("CREATE TABLE groups(id INTEGER PRIMARY KEY AUTOINCREMENT,group_name TEXT NOT NULL)")
        db.execSQL("CREATE TABLE currency(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,symbol TEXT)")
        db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER NOT NULL,type TEXT NOT NULL,amount REAL NOT NULL,currency TEXT DEFAULT 'YER',date TEXT NOT NULL,note TEXT,share_ref TEXT UNIQUE,FOREIGN KEY(customer_id) REFERENCES customers(id) ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE transactions_d(id INTEGER PRIMARY KEY AUTOINCREMENT,transaction_id INTEGER NOT NULL,item_name TEXT,quantity REAL,unit_price REAL,total_price REAL,FOREIGN KEY(transaction_id) REFERENCES transactions(id) ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE reminders(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER,reminder_date TEXT NOT NULL,note TEXT,is_completed INTEGER DEFAULT 0)")
        db.execSQL("CREATE VIEW transactions_tot_v AS SELECT c.id AS customer_id,c.name,SUM(CASE WHEN t.type='DEBIT' THEN t.amount ELSE 0 END) total_lah,SUM(CASE WHEN t.type='CREDIT' THEN t.amount ELSE 0 END) total_alayh,SUM(CASE WHEN t.type='DEBIT' THEN t.amount ELSE -t.amount END) total_amount FROM customers c LEFT JOIN transactions t ON t.customer_id=c.id GROUP BY c.id,c.name")
        db.execSQL("INSERT INTO currency(name,symbol) VALUES('ريال يمني','YER'),('ريال سعودي','SAR')")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun customers(): List<Array<String>> { val out=mutableListOf<Array<String>>(); readableDatabase.rawQuery("SELECT id,name,COALESCE(phone,''),COALESCE(balance,0) FROM customers ORDER BY name",null).use{c->while(c.moveToNext()) out.add(arrayOf(c.getString(0),c.getString(1),c.getString(2),c.getString(3)))}; return out }
    fun addCustomer(name:String,phone:String){writableDatabase.execSQL("INSERT INTO customers(name,phone) VALUES(?,?)",arrayOf(name,phone))}
    fun addTransaction(customerId:Long,type:String,amount:Double,note:String){val d=java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",java.util.Locale.US).format(java.util.Date()); writableDatabase.execSQL("INSERT INTO transactions(customer_id,type,amount,date,note) VALUES(?,?,?,?,?)",arrayOf(customerId,type,amount,d,note)); writableDatabase.execSQL("UPDATE customers SET balance=balance+? WHERE id=?",arrayOf(if(type=="DEBIT") amount else -amount,customerId))}
}
