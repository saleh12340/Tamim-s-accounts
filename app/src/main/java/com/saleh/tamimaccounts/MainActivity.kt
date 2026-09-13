package com.saleh.tamimaccounts

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var content: LinearLayout
    private val money = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2 }
    private val green = Color.rgb(20,107,80)
    private val red = Color.rgb(190,50,50)
    private val req = 400

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); db=DatabaseHelper(this); requestRuntimePermissions(); home() }
    private fun requestRuntimePermissions(){
        val p=mutableListOf<String>(); if(Build.VERSION.SDK_INT>=31){p+=Manifest.permission.BLUETOOTH_CONNECT;p+=Manifest.permission.BLUETOOTH_SCAN}; if(Build.VERSION.SDK_INT>=33)p+=Manifest.permission.POST_NOTIFICATIONS
        val needed=p.filter{checkSelfPermission(it)!=PackageManager.PERMISSION_GRANTED}; if(needed.isNotEmpty())requestPermissions(needed.toTypedArray(),req)
    }
    private fun root(title:String):LinearLayout{
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;layoutDirection=View.LAYOUT_DIRECTION_RTL;setPadding(14,12,14,8)}
        val h=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL}
        val t=TextView(this).apply{text=title;textSize=20f;setTextColor(green);gravity=Gravity.CENTER;setTypeface(null,1)}
        h.addView(t,LinearLayout.LayoutParams(0,55,1f)); h.addView(Button(this).apply{text="⋮";setOnClickListener{menu()}},LinearLayout.LayoutParams(55,55)); r.addView(h)
        content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}; r.addView(ScrollView(this).apply{addView(content);layoutParams=LinearLayout.LayoutParams(-1,0,1f)})
        val nav=LinearLayout(this).apply{gravity=Gravity.CENTER}
        listOf("الحسابات","العمليات","الأصناف","المصروفات","التقارير").forEach{label->nav.addView(Button(this).apply{text=label;setOnClickListener{when(label){"الحسابات"->home();"العمليات"->allTransactions();"الأصناف"->products();"المصروفات"->expenses();"التقارير"->reports()}}},LinearLayout.LayoutParams(0,56,1f)))}
        r.addView(nav); return r
    }
    private fun home(){setContentView(root("بقالة العزي - دفتر الحسابات")); val c=db.customers(); val total=c.sumOf{it.balance}; content.addView(TextView(this).apply{text="العملاء: ${c.size}    صافي الحسابات: ${money.format(total)}";textSize=16f;setPadding(8,10,8,10)})
        content.addView(Button(this).apply{text="+ إضافة حساب جديد";setOnClickListener{customerDialog(null)}})
        c.forEach{customer-> val card=TextView(this).apply{text="${customer.name}\n${customer.phone}\n${if(customer.balance>=0)"عليه" else "له"}: ${money.format(kotlin.math.abs(customer.balance))}";textSize=17f;setPadding(18,16,18,16);setOnClickListener{ledger(customer.id)}};content.addView(card);content.addView(divider())}
    }
    private fun divider()=Space(this).apply{minimumHeight=2}
    private fun customerDialog(old:Customer?){
        val name=EditText(this).apply{hint="اسم العميل";setText(old?.name? : "")}; val phone=EditText(this).apply{hint="رقم الهاتف";setText(old?.phone? : "");inputType=3}; val notes=EditText(this).apply{hint="ملاحظات";setText(old?.notes? : "")}
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(name);addView(phone);addView(notes)}
        AlertDialog.Builder(this).setTitle(if(old==null)"إضافة حساب" else "تعديل الحساب").setView(box).setPositiveButton("حفظ"){_,_->if(name.text.isNotBlank()){if(old==null)db.addCustomer(name.text.toString(),phone.text.toString(),notes.text.toString()) else db.updateCustomer(old.id,name.text.toString(),phone.text.toString(),notes.text.toString());home()}}.setNegativeButton("إلغاء",null).show()
    }
    private fun ledger(id:Long){val c=db.customer(id)?:return;setContentView(root("حساب: ${c.name}"));content.addView(TextView(this).apply{text="الهاتف: ${c.phone}\nالرصيد النهائي: ${money.format(c.balance)}";textSize=18f;setPadding(8,8,8,14)});val actions=LinearLayout(this);actions.addView(Button(this).apply{text="+ عملية";setOnClickListener{transactionDialog(c)}} ,LinearLayout.LayoutParams(0,60,1f));actions.addView(Button(this).apply{text="تعديل";setOnClickListener{customerDialog(c)}},LinearLayout.LayoutParams(0,60,1f));actions.addView(Button(this).apply{text="مشاركة";setOnClickListener{shareStatement(c)}},LinearLayout.LayoutParams(0,60,1f));content.addView(actions)
        db.transactions(id).forEach{tx->val v=TextView(this).apply{text="${tx.date}  |  ${if(tx.type=="DEBIT")"عليه" else "له"}  ${money.format(tx.amount)} ${tx.currency}\n${tx.note}";textSize=16f;setPadding(12,14,12,14);setOnClickListener{transactionDialog(c,tx)};setOnLongClickListener{AlertDialog.Builder(this@MainActivity).setItems(arrayOf("تعديل","حذف")){_,which->if(which==0)transactionDialog(c,tx)else{db.deleteTransaction(tx.id);ledger(id)}}.show();true}};content.addView(v);content.addView(divider())}
        content.addView(Button(this).apply{text="حذف الحساب بالكامل";setTextColor(red);setOnClickListener{confirmDelete("حذف الحساب؟ سيتم حذف جميع عملياته أيضاً"){db.deleteCustomer(id);home()}}})
    }
    private fun transactionDialog(c:Customer,old:Tx?=null){val amount=EditText(this).apply{hint="المبلغ";inputType=2 or 8192;setText(old?.amount?.toString()? : "")};val note=EditText(this).apply{hint="البيان";setText(old?.note? : "")};val currency=EditText(this).apply{hint="العملة (YER/SAR/USD)";setText(old?.currency?:"YER")};val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(amount);addView(note);addView(currency)};val b=AlertDialog.Builder(this).setTitle(if(old==null)"إضافة عملية" else "تعديل عملية").setView(box);if(old==null){b.setPositiveButton("عليه"){_,_->saveTx(c.id,"DEBIT",amount,note,currency)}.setNeutralButton("له"){_,_->saveTx(c.id,"CREDIT",amount,note,currency)}}else{b.setPositiveButton("حفظ"){_,_->val a=amount.text.toString().toDoubleOrNull();if(a!=null&&a>0){db.updateTransaction(old.id,old.type,a,note.text.toString(),currency.text.toString());ledger(c.id)}}};b.setNegativeButton("إلغاء",null).show()}
    private fun saveTx(id:Long,type:String,a:EditText,n:EditText,cur:EditText){val v=a.text.toString().toDoubleOrNull();if(v==null||v<=0){toast("أدخل مبلغاً صحيحاً");return};db.addTransaction(id,type,v,n.text.toString(),cur.text.toString().uppercase());ledger(id)}
    private fun allTransactions(){setContentView(root("كل العمليات"));db.customers().forEach{c->db.transactions(c.id).forEach{tx->content.addView(TextView(this).apply{text="${c.name} | ${tx.date} | ${if(tx.type=="DEBIT")"عليه" else "له"} | ${money.format(tx.amount)} ${tx.currency}\n${tx.note}";setPadding(10,12,10,12);setOnLongClickListener{confirmDelete("حذف العملية؟"){db.deleteTransaction(tx.id);allTransactions()};true}})}}}
    private fun products(){setContentView(root("الأصناف والمخزون"));content.addView(Button(this).apply{text="+ إضافة صنف";setOnClickListener{productDialog()}});db.products().forEach{p->content.addView(TextView(this).apply{text="${p.name}\nالمخزون: ${money.format(p.stock)}   بيع: ${money.format(p.sellPrice)}   شراء: ${money.format(p.buyPrice)}${if(p.stock<=p.minStock)"\n⚠ مخزون منخفض" else ""}";textSize=16f;setPadding(12,14,12,14);setOnLongClickListener{confirmDelete("حذف الصنف؟"){db.deleteProduct(p.id);products()};true}})}}
    private fun productDialog(){val fields=(0..5).map{EditText(this)};fields[0].hint="اسم الصنف";fields[1].hint="الباركود";fields[2].hint="سعر الشراء";fields[3].hint="سعر البيع";fields[4].hint="الكمية";fields[5].hint="الحد الأدنى";val notes=EditText(this).apply{hint="ملاحظات"};val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;fields.forEach{addView(it)};addView(notes)};AlertDialog.Builder(this).setTitle("إضافة صنف").setView(box).setPositiveButton("حفظ"){_,_->if(fields[0].text.isNotBlank())try{db.addProduct(fields[0].text.toString(),fields[1].text.toString(),fields[2].num(),fields[3].num(),fields[4].num(),fields[5].num(),notes.text.toString());products()}catch(e:Exception){toast("تعذر حفظ الصنف: ${e.message}")}}.setNegativeButton("إلغاء",null).show()}
    private fun expenses(){setContentView(root("المصروفات"));content.addView(Button(this).apply{text="+ إضافة مصروف";setOnClickListener{expenseDialog()}});db.expenses().forEach{e->content.addView(TextView(this).apply{text="${e.date} | ${e.title}\n${money.format(e.amount)} ${e.currency}\n${e.note}";setPadding(12,14,12,14);setOnLongClickListener{confirmDelete("حذف المصروف؟"){db.deleteExpense(e.id);expenses()};true}})}}
    private fun expenseDialog(){val title=EditText(this).apply{hint="اسم المصروف"};val amount=EditText(this).apply{hint="المبلغ";inputType=2 or 8192};val note=EditText(this).apply{hint="البيان"};val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(title);addView(amount);addView(note)};AlertDialog.Builder(this).setTitle("إضافة مصروف").setView(box).setPositiveButton("حفظ"){_,_->val a=amount.text.toString().toDoubleOrNull();if(title.text.isNotBlank()&&a!=null&&a>0){db.addExpense(title.text.toString(),a,note.text.toString());expenses()}}.setNegativeButton("إلغاء",null).show()}
    private fun reports(){setContentView(root("التقارير"));val c=db.customers();val debit=c.sumOf{db.transactions(it.id).filter{t->t.type=="DEBIT"&&t.currency=="YER"}.sumOf{t->t.amount}};val credit=c.sumOf{db.transactions(it.id).filter{t->t.type=="CREDIT"&&t.currency=="YER"}.sumOf{t->t.amount}};val ex=db.expenses().filter{it.currency=="YER"}.sumOf{it.amount};content.addView(TextView(this).apply{text="ملخص ريال يمني\nإجمالي عليه: ${money.format(debit)}\nإجمالي له: ${money.format(credit)}\nالمصروفات: ${money.format(ex)}\nالصافي: ${money.format(debit-credit)}";textSize=19f;setPadding(10,18,10,18)});content.addView(Button(this).apply{text="تصدير العمليات CSV";setOnClickListener{exportCsv()}})}
    private fun menu(){val items=arrayOf("نسخ احتياطي لقاعدة البيانات","استعادة قاعدة البيانات","مشاركة قاعدة البيانات","إعدادات المتجر","البلوتوث والطباعة","حول التطبيق");AlertDialog.Builder(this).setItems(items){_,w->when(w){0->backup();1->restore();2->shareDb();3->settingsDialog();4->bluetooth();5->about()}}.show()}
    private fun settingsDialog(){val name=EditText(this).apply{hint="اسم المتجر";setText(db.setting("store_name"))};val phone=EditText(this).apply{hint="الهاتف";setText(db.setting("phone"));inputType=3};val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(name);addView(phone)};AlertDialog.Builder(this).setTitle("إعدادات المتجر").setView(box).setPositiveButton("حفظ"){_,_->db.setSetting("store_name",name.text.toString());db.setSetting("phone",phone.text.toString());home()}.setNegativeButton("إلغاء",null).show()}
    private fun backup(){val i=Intent(Intent.ACTION_CREATE_DOCUMENT).apply{type="application/octet-stream";putExtra(Intent.EXTRA_TITLE,"app_database.db")};startActivityForResult(i,10)}
    private fun restore(){val i=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/octet-stream";addCategory(Intent.CATEGORY_OPENABLE)};startActivityForResult(i,11)}
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data?.data==null)return;try{when(requestCode){10->contentResolver.openOutputStream(data.data!!)?.use{out->File(filesDir,"../databases/app_database.db").canonicalFile.inputStream().use{it.copyTo(out)}};11->restoreDb(data.data!!)}}catch(e:Exception){toast("فشل: ${e.message}")}}
    private fun restoreDb(uri:Uri){val target=getDatabasePath(DatabaseHelper.DB_NAME);db.close();contentResolver.openInputStream(uri)?.use{input->target.parentFile?.mkdirs();input.copyTo(target.outputStream())};db=DatabaseHelper(this);home();toast("تمت استعادة قاعدة البيانات")}
    private fun shareDb(){val f=getDatabasePath(DatabaseHelper.DB_NAME);val uri=Uri.parse("content://com.saleh.tamimaccounts.fileprovider/${f.name}");shareText("ملف قاعدة البيانات موجود في: ${f.absolutePath}")} 
    private fun shareStatement(c:Customer){val sb=StringBuilder().append(db.setting("store_name")).append("\nحساب: ").append(c.name).append("\nالهاتف: ").append(c.phone).append("\n");db.transactions(c.id).forEach{sb.append(it.date).append(" | ").append(if(it.type=="DEBIT")"عليه" else "له").append(" | ").append(money.format(it.amount)).append(' ').append(it.currency).append(" | ").append(it.note).append('\n')};sb.append("الرصيد: ").append(money.format(c.balance));shareText(sb.toString())}
    private fun shareText(text:String){startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,text)},"مشاركة"))}
    private fun exportCsv(){val f=File(cacheDir,"transactions.csv");f.printWriter().use{p->p.println("العميل,التاريخ,النوع,المبلغ,العملة,البيان");db.customers().forEach{c->db.transactions(c.id).forEach{t->p.println("\"${c.name}\",\"${t.date}\",\"${t.type}\",${t.amount},${t.currency},\"${t.note.replace("\"","\"\"")}\"")}}};shareText(f.readText())}
    private fun bluetooth(){if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){requestRuntimePermissions();return};val enabled=BluetoothAdapter.getDefaultAdapter()?.isEnabled==true;AlertDialog.Builder(this).setTitle("الطباعة والبلوتوث").setMessage(if(enabled)"البلوتوث مفعل. يمكنك اختيار طابعة من النظام." else "البلوتوث غير مفعل.").setPositiveButton(if(enabled)"إعدادات البلوتوث" else "تشغيل البلوتوث"){_,_->startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))}.setNegativeButton("إغلاق",null).show()}
    private fun about(){AlertDialog.Builder(this).setTitle("بقالة العزي - دفتر الحسابات").setMessage("نسخة محلية تعمل بقاعدة SQLite.\nبدون حد 250 عملية.\nالهاتف: ${db.setting("phone","776425052")}").setPositiveButton("حسناً",null).show()}
    private fun confirmDelete(msg:String,yes:()->Unit){AlertDialog.Builder(this).setMessage(msg).setPositiveButton("حذف"){_,_->yes()}.setNegativeButton("إلغاء",null).show()}
    private fun toast(s:String){Toast.makeText(this,s,Toast.LENGTH_SHORT).show()}
    private fun EditText.num()=text.toString().toDoubleOrNull()?:0.0
}
