package com.saleh.tamimaccounts

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var content: LinearLayout
    private val money = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2 }
    private val green = Color.rgb(20, 107, 80)
    private val greenDark = Color.rgb(13, 77, 58)
    private val red = Color.rgb(190, 50, 50)
    private val bg = Color.rgb(247, 249, 248)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(this)
        requestRuntimePermissions()
        home()
    }

    private fun requestRuntimePermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 31) {
            permissions += Manifest.permission.BLUETOOTH_CONNECT
            permissions += Manifest.permission.BLUETOOTH_SCAN
        }
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        val needed = permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isNotEmpty()) requestPermissions(needed.toTypedArray(), 400)
    }

    private fun background(view: View) { view.setBackgroundColor(bg) }

    private fun card(): GradientDrawable = GradientDrawable().apply {
        setColor(Color.WHITE)
        cornerRadius = 22f
        setStroke(1, Color.rgb(225, 232, 228))
    }

    private fun actionButton(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = 13f
        minHeight = 0
        minimumHeight = 0
        minWidth = 0
        minimumWidth = 0
        setPadding(8, 0, 8, 0)
        setOnClickListener { onClick() }
    }

    private fun root(title: String): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            background = card()
            setPadding(10, 8, 10, 6)
        }
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(6, 2, 6, 2)
        }
        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setTypeface(null, 1)
        }
        val titleBox = FrameLayout(this).apply {
            background = GradientDrawable().apply { setColor(green); cornerRadius = 18f }
            setPadding(8, 0, 8, 0)
        }
        titleBox.addView(titleView, FrameLayout.LayoutParams(-1, 48))
        header.addView(titleBox, LinearLayout.LayoutParams(0, 48, 1f).apply { setMargins(0, 0, 8, 0) })
        header.addView(actionButton("☰", { menu() }).apply { setTextColor(greenDark) }, LinearLayout.LayoutParams(48, 48))
        root.addView(header)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(2, 8, 2, 8)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(2, 4, 2, 2)
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = 18f }
        }
        val labels = listOf("الحسابات" to { home() }, "العمليات" to { allTransactions() }, "المزيد" to { menu() })
        labels.forEach { (label, click) ->
            nav.addView(actionButton(label, click).apply { setTextColor(greenDark); setTypeface(null, 1) }, LinearLayout.LayoutParams(0, 48, 1f))
        }
        root.addView(nav)
        return root
    }

    private fun sectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(greenDark)
        setTypeface(null, 1)
        setPadding(10, 10, 10, 8)
    }

    private fun home() {
        setContentView(root("بقالة العزي - دفتر الحسابات"))
        val customers = db.customers()
        val total = customers.sumOf { it.balance }
        content.addView(sectionTitle("ملخص الحسابات"))
        val summary = TextView(this).apply {
            text = "عدد الحسابات: ${customers.size}\nصافي الأرصدة: ${money.format(total)}"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            background = card()
            setPadding(18, 14, 18, 14)
        }
        content.addView(summary, LinearLayout.LayoutParams(-1, -2).apply { setMargins(4, 0, 4, 8) })
        content.addView(actionButton("＋ إضافة حساب جديد", { customerDialog(null) }).apply { setTextColor(greenDark); setTypeface(null, 1) }, LinearLayout.LayoutParams(-1, 48).apply { setMargins(4, 0, 4, 8) })
        if (customers.isEmpty()) {
            content.addView(TextView(this).apply { text = "لا توجد حسابات بعد. اضغط إضافة حساب جديد للبدء."; textSize = 15f; gravity = Gravity.CENTER; setPadding(20, 40, 20, 40) })
        }
        customers.forEach { customer ->
            val cardView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = card()
                setPadding(16, 12, 16, 12)
                setOnClickListener { ledger(customer.id) }
            }
            cardView.addView(TextView(this).apply { text = customer.name; textSize = 17f; setTextColor(Color.rgb(35,35,35)); setTypeface(null,1) })
            cardView.addView(TextView(this).apply { text = if (customer.phone.isBlank()) "بدون رقم هاتف" else customer.phone; textSize = 13f; setTextColor(Color.GRAY); setPadding(0,4,0,4) })
            cardView.addView(TextView(this).apply { text = "${if (customer.balance >= 0) "عليه" else "له"}: ${money.format(kotlin.math.abs(customer.balance))}"; textSize = 16f; setTextColor(if (customer.balance >= 0) red else green); setTypeface(null,1) })
            content.addView(cardView, LinearLayout.LayoutParams(-1, -2).apply { setMargins(4, 4, 4, 4) })
        }
    }

    private fun customerDialog(old: Customer?) {
        val name = EditText(this).apply { hint = "اسم العميل *"; setText(old?.name ?: ""); textSize = 16f }
        val phone = EditText(this).apply { hint = "رقم الهاتف"; setText(old?.phone ?: ""); inputType = 3; textSize = 16f }
        val notes = EditText(this).apply { hint = "ملاحظات"; setText(old?.notes ?: ""); textSize = 16f }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(10, 0, 10, 0); addView(name); addView(phone); addView(notes) }
        AlertDialog.Builder(this).setTitle(if (old == null) "إضافة حساب" else "تعديل الحساب").setView(box)
            .setPositiveButton("حفظ") { _, _ ->
                if (name.text.isBlank()) toast("اكتب اسم العميل أولاً")
                else if (old == null) { db.addCustomer(name.text.toString(), phone.text.toString(), notes.text.toString()); home() }
                else { db.updateCustomer(old.id, name.text.toString(), phone.text.toString(), notes.text.toString()); home() }
            }.setNegativeButton("إلغاء", null).show()
    }

    private fun ledger(id: Long) {
        val customer = db.customer(id) ?: return
        setContentView(root("حساب ${customer.name}"))
        content.addView(sectionTitle("بيانات الحساب"))
        content.addView(TextView(this).apply { text = "الهاتف: ${if (customer.phone.isBlank()) "—" else customer.phone}\nالرصيد: ${money.format(customer.balance)}"; textSize = 17f; setPadding(12,8,12,14) })
        val actions = LinearLayout(this)
        listOf("＋ عملية" to { transactionDialog(customer) }, "تعديل" to { customerDialog(customer) }, "مشاركة" to { shareStatement(customer) }).forEach { (t, f) -> actions.addView(actionButton(t, f), LinearLayout.LayoutParams(0, 46, 1f).apply { setMargins(2,0,2,0) }) }
        content.addView(actions)
        content.addView(sectionTitle("سجل العمليات"))
        val txs = db.transactions(id)
        if (txs.isEmpty()) content.addView(TextView(this).apply { text = "لا توجد عمليات لهذا الحساب."; gravity = Gravity.CENTER; setPadding(10,25,10,25) })
        txs.forEach { tx ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = card(); setPadding(14,10,14,10); setOnClickListener { transactionDialog(customer,tx) } }
            row.addView(TextView(this).apply { text = "${if(tx.type=="DEBIT") "عليه" else "له"}  •  ${money.format(tx.amount)} ${tx.currency}"; textSize=16f; setTypeface(null,1); setTextColor(if(tx.type=="DEBIT") red else green) })
            row.addView(TextView(this).apply { text = "${tx.date}\n${tx.note}"; textSize=13f; setTextColor(Color.DKGRAY); setPadding(0,4,0,0) })
            row.setOnLongClickListener { AlertDialog.Builder(this@MainActivity).setItems(arrayOf("تعديل العملية","حذف العملية")){_,w->if(w==0) transactionDialog(customer,tx) else confirmDelete("حذف العملية؟"){db.deleteTransaction(tx.id);ledger(id)}}.show(); true }
            content.addView(row, LinearLayout.LayoutParams(-1,-2).apply { setMargins(4,4,4,4) })
        }
        content.addView(actionButton("حذف الحساب بالكامل", { confirmDelete("سيتم حذف الحساب وجميع عملياته. هل تريد المتابعة؟"){db.deleteCustomer(id);home()} }).apply { setTextColor(red) }, LinearLayout.LayoutParams(-1,48).apply { setMargins(4,12,4,4) })
    }

    private fun transactionDialog(customer: Customer, old: Tx? = null) {
        val amount=EditText(this).apply{hint="المبلغ *";inputType=2 or 8192;setText(old?.amount?.toString() ?: "")}
        val note=EditText(this).apply{hint="البيان";setText(old?.note ?: "")}
        val currency=EditText(this).apply{hint="العملة";setText(old?.currency ?: "YER")}
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(10,0,10,0);addView(amount);addView(note);addView(currency)}
        val b=AlertDialog.Builder(this).setTitle(if(old==null)"إضافة عملية" else "تعديل عملية").setView(box)
        if(old==null)b.setPositiveButton("عليه"){_,_->saveTx(customer.id,"DEBIT",amount,note,currency)}.setNeutralButton("له"){_,_->saveTx(customer.id,"CREDIT",amount,note,currency)}
        else b.setPositiveButton("حفظ"){_,_->val v=amount.text.toString().toDoubleOrNull();if(v!=null&&v>0){db.updateTransaction(old.id,old.type,v,note.text.toString(),currency.text.toString().trim().uppercase());ledger(customer.id)}else toast("أدخل مبلغاً صحيحاً")}
        b.setNegativeButton("إلغاء",null).show()
    }
    private fun saveTx(id:Long,type:String,a:EditText,n:EditText,c:EditText){val v=a.text.toString().toDoubleOrNull();if(v==null||v<=0){toast("أدخل مبلغاً صحيحاً");return};db.addTransaction(id,type,v,n.text.toString(),c.text.toString().trim().uppercase());ledger(id)}

    private fun allTransactions(){setContentView(root("كل العمليات"));val customers=db.customers();if(customers.isEmpty())content.addView(TextView(this).apply{text="لا توجد عمليات بعد.";gravity=Gravity.CENTER;setPadding(10,40,10,40)});customers.forEach{c->db.transactions(c.id).forEach{tx->val row=TextView(this).apply{text="${c.name}\n${tx.date}  •  ${if(tx.type=="DEBIT")"عليه" else "له"}  ${money.format(tx.amount)} ${tx.currency}\n${tx.note}";textSize=15f;background=card();setPadding(14,12,14,12);setOnLongClickListener{confirmDelete("حذف العملية؟"){db.deleteTransaction(tx.id);allTransactions()};true}};content.addView(row,LinearLayout.LayoutParams(-1,-2).apply{setMargins(4,4,4,4)})}}}

    private fun products(){setContentView(root("الأصناف والمخزون"));content.addView(actionButton("＋ إضافة صنف",{productDialog()}));db.products().forEach{p->val row=TextView(this).apply{text="${p.name}\nالمخزون: ${money.format(p.stock)}  •  بيع: ${money.format(p.sellPrice)}  •  شراء: ${money.format(p.buyPrice)}${if(p.stock<=p.minStock)"\n⚠ المخزون منخفض" else ""}";textSize=15f;background=card();setPadding(14,12,14,12);setOnLongClickListener{confirmDelete("حذف الصنف؟"){db.deleteProduct(p.id);products()};true}};content.addView(row,LinearLayout.LayoutParams(-1,-2).apply{setMargins(4,4,4,4)})}}
    private fun productDialog(){val f=(0..5).map{EditText(this)};f[0].hint="اسم الصنف *";f[1].hint="الباركود";f[2].hint="سعر الشراء";f[3].hint="سعر البيع";f[4].hint="الكمية";f[5].hint="الحد الأدنى";for(i in 2..5)f[i].inputType=2 or 8192;val n=EditText(this).apply{hint="ملاحظات"};val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(10,0,10,0);f.forEach{addView(it)};addView(n)};AlertDialog.Builder(this).setTitle("إضافة صنف").setView(box).setPositiveButton("حفظ"){_,_->if(f[0].text.isBlank())toast("اكتب اسم الصنف")else try{db.addProduct(f[0].text.toString(),f[1].text.toString(),f[2].num(),f[3].num(),f[4].num(),f[5].num(),n.text.toString());products()}catch(e:Exception){toast("تعذر حفظ الصنف")}}.setNegativeButton("إلغاء",null).show()}

    private fun expenses(){setContentView(root("المصروفات"));content.addView(actionButton("＋ إضافة مصروف",{expenseDialog()}));db.expenses().forEach{e->val row=TextView(this).apply{text="${e.title}\n${money.format(e.amount)} ${e.currency}  •  ${e.date}\n${e.note}";textSize=15f;background=card();setPadding(14,12,14,12);setOnLongClickListener{confirmDelete("حذف المصروف؟"){db.deleteExpense(e.id);expenses()};true}};content.addView(row,LinearLayout.LayoutParams(-1,-2).apply{setMargins(4,4,4,4)})}}
    private fun expenseDialog(){val t=EditText(this).apply{hint="اسم المصروف *"};val a=EditText(this).apply{hint="المبلغ *";inputType=2 or 8192};val n=EditText(this).apply{hint="البيان"};val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(10,0,10,0);addView(t);addView(a);addView(n)};AlertDialog.Builder(this).setTitle("إضافة مصروف").setView(box).setPositiveButton("حفظ"){_,_->val v=a.text.toString().toDoubleOrNull();if(t.text.isNotBlank()&&v!=null&&v>0){db.addExpense(t.text.toString(),v,n.text.toString());expenses()}else toast("أدخل البيانات بشكل صحيح")}.setNegativeButton("إلغاء",null).show()}

    private fun reports(){setContentView(root("التقارير"));val c=db.customers();val debit=c.sumOf{db.transactions(it.id).filter{x->x.type=="DEBIT"&&x.currency=="YER"}.sumOf{it.amount}};val credit=c.sumOf{db.transactions(it.id).filter{x->x.type=="CREDIT"&&x.currency=="YER"}.sumOf{it.amount}};val ex=db.expenses().filter{it.currency=="YER"}.sumOf{it.amount};content.addView(sectionTitle("ملخص ريال يمني"));content.addView(TextView(this).apply{text="عليه: ${money.format(debit)}\nله: ${money.format(credit)}\nالمصروفات: ${money.format(ex)}\nصافي العمليات: ${money.format(debit-credit)}";textSize=18f;background=card();setPadding(18,16,18,16)})}

    private fun menu(){val items=arrayOf("الأصناف والمخزون","المصروفات","التقارير","نسخ احتياطي آمن","استعادة قاعدة البيانات","مشاركة قاعدة البيانات","إعدادات المتجر","البلوتوث والطباعة","حول التطبيق");AlertDialog.Builder(this).setTitle("القائمة الرئيسية").setItems(items){_,w->when(w){0->products();1->expenses();2->reports();3->backup();4->restore();5->shareDb();6->settingsDialog();7->bluetooth();8->about()}}.show()}

    private fun settingsDialog(){val name=EditText(this).apply{hint="اسم المتجر";setText(db.setting("store_name"))};val phone=EditText(this).apply{hint="رقم الهاتف";setText(db.setting("phone","776425052"));inputType=3};val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(10,0,10,0);addView(name);addView(phone)};AlertDialog.Builder(this).setTitle("إعدادات المتجر").setView(box).setPositiveButton("حفظ"){_,_->db.setSetting("store_name",name.text.toString());db.setSetting("phone",phone.text.toString());home()}.setNegativeButton("إلغاء",null).show()}

    private fun backup(){try{db.close();val source=getDatabasePath(DatabaseHelper.DB_NAME);if(!source.exists()){db=DatabaseHelper(this);toast("قاعدة البيانات غير موجودة");return};startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{type="application/x-sqlite3";addCategory(Intent.CATEGORY_OPENABLE);putExtra(Intent.EXTRA_TITLE,"app_database.db")},10)}catch(e:Exception){db=DatabaseHelper(this);toast("تعذر تجهيز النسخة الاحتياطية")}}
    private fun restore(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="*/*";addCategory(Intent.CATEGORY_OPENABLE);putExtra(Intent.EXTRA_MIME_TYPES,arrayOf("application/x-sqlite3","application/octet-stream","application/vnd.sqlite3"))},11)}
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data?.data==null){if(requestCode==10)db=DatabaseHelper(this);return};try{if(requestCode==10)saveBackup(data.data!!) else if(requestCode==11)restoreDb(data.data!!)}catch(e:Exception){db=DatabaseHelper(this);toast("فشل: ${e.message}")}}

    private fun saveBackup(uri:Uri){try{val source=getDatabasePath(DatabaseHelper.DB_NAME);if(!source.exists())throw IllegalStateException("قاعدة البيانات غير موجودة");contentResolver.openOutputStream(uri)?.use{out->source.inputStream().use{input->input.copyTo(out)}}?:throw IllegalStateException("تعذر فتح ملف الحفظ");db=DatabaseHelper(this);toast("تم حفظ النسخة الاحتياطية بنجاح")}catch(e:Exception){db=DatabaseHelper(this);toast("فشل النسخ الاحتياطي: ${e.message}")}}

    private fun restoreDb(uri:Uri){val temp=File(cacheDir,"restore_${System.currentTimeMillis()}.db");try{contentResolver.openInputStream(uri)?.use{input->temp.outputStream().use{out->input.copyTo(out)}}?:throw IllegalStateException("تعذر قراءة الملف");if(temp.length()<1024)throw IllegalStateException("الملف صغير أو غير صالح");SQLiteDatabase.openDatabase(temp.absolutePath,null,SQLiteDatabase.OPEN_READONLY).use{test->test.rawQuery("PRAGMA integrity_check",null).use{c->if(!c.moveToFirst()||!c.getString(0).equals("ok",true))throw IllegalStateException("قاعدة البيانات تالفة")}};db.close();val target=getDatabasePath(DatabaseHelper.DB_NAME);File(target.absolutePath+"-wal").delete();File(target.absolutePath+"-shm").delete();if(!temp.renameTo(target)){temp.inputStream().use{input->target.outputStream().use{out->input.copyTo(out)}};temp.delete()};db=DatabaseHelper(this);db.readableDatabase;home();toast("تمت استعادة قاعدة البيانات بنجاح")}catch(e:Exception){temp.delete();db=DatabaseHelper(this);home();toast("فشل الاستعادة: ${e.message}")}}

    private fun shareDb(){val source=getDatabasePath(DatabaseHelper.DB_NAME);if(!source.exists()){toast("لا توجد قاعدة بيانات");return};try{val f=File(cacheDir,"app_database_share.db");source.inputStream().use{input->f.outputStream().use{out->input.copyTo(out)}};shareText("نسخة قاعدة البيانات موجودة في: ${f.name}")}catch(e:Exception){toast("تعذر تجهيز المشاركة")}}
    private fun shareStatement(c:Customer){val s=StringBuilder().append(db.setting("store_name")).append("\nحساب: ").append(c.name).append("\nالهاتف: ").append(c.phone).append("\n");db.transactions(c.id).forEach{t->s.append(t.date).append(" | ").append(if(t.type=="DEBIT")"عليه" else "له").append(" | ").append(money.format(t.amount)).append(' ').append(t.currency).append(" | ").append(t.note).append('\n')};s.append("الرصيد: ").append(money.format(c.balance));shareText(s.toString())}
    private fun shareText(text:String){startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,text)},"مشاركة"))}

    private fun bluetooth(){if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){requestRuntimePermissions();return};val enabled=BluetoothAdapter.getDefaultAdapter()?.isEnabled==true;AlertDialog.Builder(this).setTitle("البلوتوث والطباعة").setMessage(if(enabled)"البلوتوث مفعل. من إعدادات الجهاز قم بإقران الطابعة أولاً." else "البلوتوث غير مفعل.").setPositiveButton("إعدادات البلوتوث"){_,_->startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))}.setNegativeButton("إغلاق",null).show()}
    private fun about(){AlertDialog.Builder(this).setTitle("بقالة العزي - دفتر الحسابات").setMessage("دفتر حسابات محلي يعمل بقاعدة SQLite.\nلا يوجد حد 250 عملية.\nالهاتف: ${db.setting("phone","776425052")}").setPositiveButton("حسناً",null).show()}
    private fun confirmDelete(msg:String,yes:()->Unit){AlertDialog.Builder(this).setMessage(msg).setPositiveButton("حذف"){_,_->yes()}.setNegativeButton("إلغاء",null).show()}
    private fun toast(s:String){Toast.makeText(this,s,Toast.LENGTH_LONG).show()}
    private fun EditText.num()=text.toString().toDoubleOrNull()?:0.0
}
