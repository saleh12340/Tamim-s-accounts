package com.saleh.tamimaccounts

import android.app.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity: AppCompatActivity(){
 private lateinit var db:DatabaseHelper; private lateinit var list:LinearLayout
 override fun onCreate(b:Bundle?){super.onCreate(b); db=DatabaseHelper(this); showAccounts()}
 private fun base(title:String):LinearLayout{val root=LinearLayout(this);root.orientation=LinearLayout.VERTICAL;root.layoutDirection=View.LAYOUT_DIRECTION_RTL;root.setPadding(20,20,20,12)
  val h=TextView(this);h.text=title+"\nهاتف: 726425052";h.textSize=22f;h.setPadding(0,0,0,16);root.addView(h)
  list=LinearLayout(this);list.orientation=LinearLayout.VERTICAL;root.addView(ScrollView(this).apply{addView(list);layoutParams=LinearLayout.LayoutParams(-1,0,1f)})
  val nav=LinearLayout(this);nav.gravity=Gravity.CENTER; listOf("الإعدادات","الحسابات","العمليات").forEachIndexed{ i,s->val x=Button(this);x.text=s;x.setOnClickListener{if(i==1)showAccounts() else Toast.makeText(this,s,Toast.LENGTH_SHORT).show()};nav.addView(x,LinearLayout.LayoutParams(0,60,1f))};root.addView(nav);return root}
 private fun showAccounts(){setContentView(base("بقالة العزي - دفتر الحسابات"));list.addView(Button(this).apply{text="+ إضافة حساب";setOnClickListener{addCustomer()}});db.customers().forEach{a->val v=TextView(this);v.text="${a[1]}\n${a[2]}   الرصيد: ${a[3]}";v.textSize=18f;v.setPadding(14,18,14,18);v.setOnClickListener{addTransaction(a[0].toLong(),a[1])};list.addView(v)}}
 private fun addCustomer(){val n=EditText(this);n.hint="اسم العميل";val p=EditText(this);p.hint="رقم الهاتف";LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(n);addView(p);AlertDialog.Builder(this@MainActivity).setTitle("إضافة حساب").setView(this).setPositiveButton("حفظ"){_,_->if(n.text.isNotBlank()){db.addCustomer(n.text.toString(),p.text.toString());showAccounts()}}.setNegativeButton("إلغاء",null).show()}}
 private fun addTransaction(id:Long,name:String){val a=EditText(this);a.hint="المبلغ";a.inputType=2;val note=EditText(this);note.hint="البيان";val box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.addView(a);box.addView(note);AlertDialog.Builder(this).setTitle("عملية لـ $name").setView(box).setPositiveButton("مدين"){_,_->db.addTransaction(id,"DEBIT",a.text.toString().toDoubleOrNull()?:0.0,note.text.toString());showAccounts()}.setNeutralButton("دائن"){_,_->db.addTransaction(id,"CREDIT",a.text.toString().toDoubleOrNull()?:0.0,note.text.toString());showAccounts()}.setNegativeButton("إلغاء",null).show()}
}
