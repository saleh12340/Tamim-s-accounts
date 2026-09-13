package com.saleh.tamimaccounts

import android.content.Context
import android.net.Uri
import android.database.sqlite.SQLiteDatabase
import java.io.File

object DatabaseImportManager {
 data class Result(val ok:Boolean,val message:String)
 fun importDb(c:Context,u:Uri):Result{
  val tmp=File(c.cacheDir,"import.db")
  return try{
   c.contentResolver.openInputStream(u)?.use{input->tmp.outputStream().use{output->input.copyTo(output)}}?:return Result(false,"تعذر قراءة الملف")
   if(!sqlite(tmp)||!check(tmp)||!table(tmp,"customers")||!table(tmp,"transactions")) return Result(false,"فشل التحقق من قاعدة البيانات أو الجداول المطلوبة")
   val h=DatabaseHelper(c);h.close();val target=c.getDatabasePath(DatabaseHelper.DB_NAME);val backup=File(target.path+".backup")
   if(target.exists())target.copyTo(backup,true);tmp.copyTo(target,true)
   File(target.path+"-wal").delete();File(target.path+"-shm").delete();File(target.path+"-journal").delete()
   val ok=table(target,"customers")&&table(target,"transactions")
   if(ok){backup.delete();Result(true,"تم استيراد قاعدة البيانات بنجاح")}else{if(backup.exists())backup.copyTo(target,true);Result(false,"فشل التحقق بعد الاستبدال وتمت استعادة النسخة السابقة")}
  }catch(e:Exception){Result(false,"فشل الاستيراد: "+(e.message?:"خطأ غير معروف"))}finally{tmp.delete()}
 }
 private fun sqlite(f:File)=try{f.inputStream().use{val b=ByteArray(16);it.read(b)==16&&String(b,0,6,Charsets.US_ASCII)=="SQLite"}}catch(_:Exception){false}
 private fun check(f:File)=try{SQLiteDatabase.openDatabase(f.path,null,SQLiteDatabase.OPEN_READONLY).use{d->d.rawQuery("PRAGMA integrity_check",null).use{it.moveToFirst()&&it.getString(0)=="ok"}}}catch(_:Exception){false}
 private fun table(f:File,n:String)=try{SQLiteDatabase.openDatabase(f.path,null,SQLiteDatabase.OPEN_READONLY).use{d->d.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?",arrayOf(n)).use{it.moveToFirst()}}}catch(_:Exception){false}
}
