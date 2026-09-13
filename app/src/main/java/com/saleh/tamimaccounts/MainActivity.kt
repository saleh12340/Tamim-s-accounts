package com.saleh.tamimaccounts

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
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
    private val green = Color.rgb(20, 107, 80)
    private val red = Color.rgb(190, 50, 50)

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

    private fun root(title: String): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(14, 12, 14, 8)
        }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val titleView = TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(green)
            gravity = Gravity.CENTER
            setTypeface(null, 1)
        }
        header.addView(titleView, LinearLayout.LayoutParams(0, 55, 1f))
        header.addView(Button(this).apply {
            text = "⋮"
            setOnClickListener { menu() }
        }, LinearLayout.LayoutParams(55, 55))
        root.addView(header)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(ScrollView(this).apply {
            addView(content)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        })

        val nav = LinearLayout(this).apply { gravity = Gravity.CENTER }
        val labels = listOf("الحسابات", "العمليات", "الأصناف", "المصروفات", "التقارير")
        labels.forEach { label ->
            val button = Button(this).apply {
                text = label
                setOnClickListener {
                    when (label) {
                        "الحسابات" -> home()
                        "العمليات" -> allTransactions()
                        "الأصناف" -> products()
                        "المصروفات" -> expenses()
                        "التقارير" -> reports()
                    }
                }
            }
            nav.addView(button, LinearLayout.LayoutParams(0, 56, 1f))
        }
        root.addView(nav)
        return root
    }

    private fun home() {
        setContentView(root("بقالة العزي - دفتر الحسابات"))
        val customers = db.customers()
        val total = customers.sumOf { it.balance }
        content.addView(TextView(this).apply {
            text = "العملاء: ${customers.size}    صافي الحسابات: ${money.format(total)}"
            textSize = 16f
            setPadding(8, 10, 8, 10)
        })
        content.addView(Button(this).apply {
            text = "+ إضافة حساب جديد"
            setOnClickListener { customerDialog(null) }
        })
        customers.forEach { customer ->
            val card = TextView(this).apply {
                text = "${customer.name}\n${customer.phone}\n${if (customer.balance >= 0) "عليه" else "له"}: ${money.format(kotlin.math.abs(customer.balance))}"
                textSize = 17f
                setPadding(18, 16, 18, 16)
                setOnClickListener { ledger(customer.id) }
            }
            content.addView(card)
            content.addView(divider())
        }
    }

    private fun divider() = Space(this).apply { minimumHeight = 2 }

    private fun customerDialog(old: Customer?) {
        val name = EditText(this).apply {
            hint = "اسم العميل"
            setText(old?.name ?: "")
        }
        val phone = EditText(this).apply {
            hint = "رقم الهاتف"
            setText(old?.phone ?: "")
            inputType = 3
        }
        val notes = EditText(this).apply {
            hint = "ملاحظات"
            setText(old?.notes ?: "")
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(name)
            addView(phone)
            addView(notes)
        }
        AlertDialog.Builder(this)
            .setTitle(if (old == null) "إضافة حساب" else "تعديل الحساب")
            .setView(box)
            .setPositiveButton("حفظ") { _, _ ->
                if (name.text.isNotBlank()) {
                    if (old == null) db.addCustomer(name.text.toString(), phone.text.toString(), notes.text.toString())
                    else db.updateCustomer(old.id, name.text.toString(), phone.text.toString(), notes.text.toString())
                    home()
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun ledger(id: Long) {
        val customer = db.customer(id) ?: return
        setContentView(root("حساب: ${customer.name}"))
        content.addView(TextView(this).apply {
            text = "الهاتف: ${customer.phone}\nالرصيد النهائي: ${money.format(customer.balance)}"
            textSize = 18f
            setPadding(8, 8, 8, 14)
        })
        val actions = LinearLayout(this)
        actions.addView(Button(this).apply {
            text = "+ عملية"
            setOnClickListener { transactionDialog(customer) }
        }, LinearLayout.LayoutParams(0, 60, 1f))
        actions.addView(Button(this).apply {
            text = "تعديل"
            setOnClickListener { customerDialog(customer) }
        }, LinearLayout.LayoutParams(0, 60, 1f))
        actions.addView(Button(this).apply {
            text = "مشاركة"
            setOnClickListener { shareStatement(customer) }
        }, LinearLayout.LayoutParams(0, 60, 1f))
        content.addView(actions)

        db.transactions(id).forEach { tx ->
            val row = TextView(this).apply {
                text = "${tx.date}  |  ${if (tx.type == "DEBIT") "عليه" else "له"}  ${money.format(tx.amount)} ${tx.currency}\n${tx.note}"
                textSize = 16f
                setPadding(12, 14, 12, 14)
                setOnClickListener { transactionDialog(customer, tx) }
                setOnLongClickListener {
                    AlertDialog.Builder(this@MainActivity)
                        .setItems(arrayOf("تعديل", "حذف")) { _, which ->
                            if (which == 0) {
                                transactionDialog(customer, tx)
                            } else {
                                confirmDelete("حذف العملية؟") {
                                    db.deleteTransaction(tx.id)
                                    ledger(id)
                                }
                            }
                        }.show()
                    true
                }
            }
            content.addView(row)
            content.addView(divider())
        }
        content.addView(Button(this).apply {
            text = "حذف الحساب بالكامل"
            setTextColor(red)
            setOnClickListener {
                confirmDelete("حذف الحساب؟ سيتم حذف جميع عملياته أيضاً") {
                    db.deleteCustomer(id)
                    home()
                }
            }
        })
    }

    private fun transactionDialog(customer: Customer, old: Tx? = null) {
        val amount = EditText(this).apply {
            hint = "المبلغ"
            inputType = 2 or 8192
            setText(old?.amount?.toString() ?: "")
        }
        val note = EditText(this).apply {
            hint = "البيان"
            setText(old?.note ?: "")
        }
        val currency = EditText(this).apply {
            hint = "العملة (YER/SAR/USD)"
            setText(old?.currency ?: "YER")
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(amount)
            addView(note)
            addView(currency)
        }
        val builder = AlertDialog.Builder(this)
            .setTitle(if (old == null) "إضافة عملية" else "تعديل عملية")
            .setView(box)
        if (old == null) {
            builder.setPositiveButton("عليه") { _, _ -> saveTx(customer.id, "DEBIT", amount, note, currency) }
            builder.setNeutralButton("له") { _, _ -> saveTx(customer.id, "CREDIT", amount, note, currency) }
        } else {
            builder.setPositiveButton("حفظ") { _, _ ->
                val value = amount.text.toString().toDoubleOrNull()
                if (value != null && value > 0) {
                    db.updateTransaction(old.id, old.type, value, note.text.toString(), currency.text.toString().uppercase())
                    ledger(customer.id)
                }
            }
        }
        builder.setNegativeButton("إلغاء", null).show()
    }

    private fun saveTx(id: Long, type: String, amount: EditText, note: EditText, currency: EditText) {
        val value = amount.text.toString().toDoubleOrNull()
        if (value == null || value <= 0) {
            toast("أدخل مبلغاً صحيحاً")
            return
        }
        db.addTransaction(id, type, value, note.text.toString(), currency.text.toString().uppercase())
        ledger(id)
    }

    private fun allTransactions() {
        setContentView(root("كل العمليات"))
        db.customers().forEach { customer ->
            db.transactions(customer.id).forEach { tx ->
                val row = TextView(this).apply {
                    text = "${customer.name} | ${tx.date} | ${if (tx.type == "DEBIT") "عليه" else "له"} | ${money.format(tx.amount)} ${tx.currency}\n${tx.note}"
                    setPadding(10, 12, 10, 12)
                    setOnLongClickListener {
                        confirmDelete("حذف العملية؟") {
                            db.deleteTransaction(tx.id)
                            allTransactions()
                        }
                        true
                    }
                }
                content.addView(row)
                content.addView(divider())
            }
        }
    }

    private fun products() {
        setContentView(root("الأصناف والمخزون"))
        content.addView(Button(this).apply {
            text = "+ إضافة صنف"
            setOnClickListener { productDialog() }
        })
        db.products().forEach { product ->
            val row = TextView(this).apply {
                text = "${product.name}\nالمخزون: ${money.format(product.stock)}   بيع: ${money.format(product.sellPrice)}   شراء: ${money.format(product.buyPrice)}${if (product.stock <= product.minStock) "\n⚠ مخزون منخفض" else ""}"
                textSize = 16f
                setPadding(12, 14, 12, 14)
                setOnLongClickListener {
                    confirmDelete("حذف الصنف؟") {
                        db.deleteProduct(product.id)
                        products()
                    }
                    true
                }
            }
            content.addView(row)
            content.addView(divider())
        }
    }

    private fun productDialog() {
        val fields = (0..5).map { EditText(this) }
        fields[0].hint = "اسم الصنف"
        fields[1].hint = "الباركود"
        fields[2].hint = "سعر الشراء"
        fields[3].hint = "سعر البيع"
        fields[4].hint = "الكمية"
        fields[5].hint = "الحد الأدنى"
        fields[2].inputType = 2 or 8192
        fields[3].inputType = 2 or 8192
        fields[4].inputType = 2 or 8192
        fields[5].inputType = 2 or 8192
        val notes = EditText(this).apply { hint = "ملاحظات" }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            fields.forEach { addView(it) }
            addView(notes)
        }
        AlertDialog.Builder(this)
            .setTitle("إضافة صنف")
            .setView(box)
            .setPositiveButton("حفظ") { _, _ ->
                if (fields[0].text.isNotBlank()) {
                    try {
                        db.addProduct(fields[0].text.toString(), fields[1].text.toString(), fields[2].num(), fields[3].num(), fields[4].num(), fields[5].num(), notes.text.toString())
                        products()
                    } catch (e: Exception) {
                        toast("تعذر حفظ الصنف: ${e.message}")
                    }
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun expenses() {
        setContentView(root("المصروفات"))
        content.addView(Button(this).apply {
            text = "+ إضافة مصروف"
            setOnClickListener { expenseDialog() }
        })
        db.expenses().forEach { expense ->
            val row = TextView(this).apply {
                text = "${expense.date} | ${expense.title}\n${money.format(expense.amount)} ${expense.currency}\n${expense.note}"
                setPadding(12, 14, 12, 14)
                setOnLongClickListener {
                    confirmDelete("حذف المصروف؟") {
                        db.deleteExpense(expense.id)
                        expenses()
                    }
                    true
                }
            }
            content.addView(row)
            content.addView(divider())
        }
    }

    private fun expenseDialog() {
        val title = EditText(this).apply { hint = "اسم المصروف" }
        val amount = EditText(this).apply { hint = "المبلغ"; inputType = 2 or 8192 }
        val note = EditText(this).apply { hint = "البيان" }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(title)
            addView(amount)
            addView(note)
        }
        AlertDialog.Builder(this)
            .setTitle("إضافة مصروف")
            .setView(box)
            .setPositiveButton("حفظ") { _, _ ->
                val value = amount.text.toString().toDoubleOrNull()
                if (title.text.isNotBlank() && value != null && value > 0) {
                    db.addExpense(title.text.toString(), value, note.text.toString())
                    expenses()
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun reports() {
        setContentView(root("التقارير"))
        val customers = db.customers()
        val debit = customers.sumOf { customer -> db.transactions(customer.id).filter { it.type == "DEBIT" && it.currency == "YER" }.sumOf { it.amount } }
        val credit = customers.sumOf { customer -> db.transactions(customer.id).filter { it.type == "CREDIT" && it.currency == "YER" }.sumOf { it.amount } }
        val expenses = db.expenses().filter { it.currency == "YER" }.sumOf { it.amount }
        content.addView(TextView(this).apply {
            text = "ملخص ريال يمني\nإجمالي عليه: ${money.format(debit)}\nإجمالي له: ${money.format(credit)}\nالمصروفات: ${money.format(expenses)}\nالصافي: ${money.format(debit - credit)}"
            textSize = 19f
            setPadding(10, 18, 10, 18)
        })
        content.addView(Button(this).apply {
            text = "تصدير العمليات CSV"
            setOnClickListener { exportCsv() }
        })
    }

    private fun menu() {
        val items = arrayOf("نسخ احتياطي لقاعدة البيانات", "استعادة قاعدة البيانات", "مشاركة قاعدة البيانات", "إعدادات المتجر", "البلوتوث والطباعة", "حول التطبيق")
        AlertDialog.Builder(this).setItems(items) { _, which ->
            when (which) {
                0 -> backup()
                1 -> restore()
                2 -> shareDb()
                3 -> settingsDialog()
                4 -> bluetooth()
                5 -> about()
            }
        }.show()
    }

    private fun settingsDialog() {
        val name = EditText(this).apply { hint = "اسم المتجر"; setText(db.setting("store_name")) }
        val phone = EditText(this).apply { hint = "الهاتف"; setText(db.setting("phone")); inputType = 3 }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(name)
            addView(phone)
        }
        AlertDialog.Builder(this)
            .setTitle("إعدادات المتجر")
            .setView(box)
            .setPositiveButton("حفظ") { _, _ ->
                db.setSetting("store_name", name.text.toString())
                db.setSetting("phone", phone.text.toString())
                home()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun backup() {
        startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "app_database.db")
        }, 10)
    }

    private fun restore() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/octet-stream"
            addCategory(Intent.CATEGORY_OPENABLE)
        }, 11)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data ?: return
        if (resultCode != RESULT_OK) return
        try {
            if (requestCode == 10) {
                val src = getDatabasePath(DatabaseHelper.DB_NAME)
                contentResolver.openOutputStream(uri)?.use { output ->
                    src.inputStream().use { input -> input.copyTo(output) }
                }
                toast("تم حفظ النسخة الاحتياطية")
            } else if (requestCode == 11) {
                restoreDb(uri)
            }
        } catch (e: Exception) {
            toast("فشل: ${e.message}")
        }
    }

    private fun restoreDb(uri: Uri) {
        val temp = File(cacheDir, "restore.db")
        contentResolver.openInputStream(uri)?.use { input -> temp.outputStream().use { output -> input.copyTo(output) } }
        if (!db.integrityCheck(temp.absolutePath)) {
            temp.delete()
            toast("الملف ليس قاعدة SQLite سليمة")
            return
        }
        val target = getDatabasePath(DatabaseHelper.DB_NAME)
        db.close()
        temp.inputStream().use { input -> target.outputStream().use { output -> input.copyTo(output) } }
        temp.delete()
        db = DatabaseHelper(this)
        home()
        toast("تمت استعادة قاعدة البيانات")
    }

    private fun shareDb() {
        shareText("قاعدة البيانات المحلية: ${getDatabasePath(DatabaseHelper.DB_NAME).absolutePath}")
    }

    private fun shareStatement(customer: Customer) {
        val text = StringBuilder()
            .append(db.setting("store_name"))
            .append("\nحساب: ").append(customer.name)
            .append("\nالهاتف: ").append(customer.phone).append("\n")
        db.transactions(customer.id).forEach { tx ->
            text.append(tx.date).append(" | ")
                .append(if (tx.type == "DEBIT") "عليه" else "له")
                .append(" | ").append(money.format(tx.amount)).append(' ').append(tx.currency)
                .append(" | ").append(tx.note).append('\n')
        }
        text.append("الرصيد: ").append(money.format(customer.balance))
        shareText(text.toString())
    }

    private fun shareText(text: String) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, "مشاركة"))
    }

    private fun exportCsv() {
        val file = File(cacheDir, "transactions.csv")
        file.printWriter().use { out ->
            out.println("العميل,التاريخ,النوع,المبلغ,العملة,البيان")
            db.customers().forEach { customer ->
                db.transactions(customer.id).forEach { tx ->
                    val note = tx.note.replace("\"", "\"\"")
                    out.println("\"${customer.name}\",\"${tx.date}\",\"${tx.type}\",${tx.amount},${tx.currency},\"$note\"")
                }
            }
        }
        shareText(file.readText())
    }

    private fun bluetooth() {
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestRuntimePermissions()
            return
        }
        val enabled = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
        AlertDialog.Builder(this)
            .setTitle("الطباعة والبلوتوث")
            .setMessage(if (enabled) "البلوتوث مفعل. يمكنك اختيار طابعة من إعدادات النظام." else "البلوتوث غير مفعل.")
            .setPositiveButton("إعدادات البلوتوث") { _, _ -> startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
            .setNegativeButton("إغلاق", null)
            .show()
    }

    private fun about() {
        AlertDialog.Builder(this)
            .setTitle("بقالة العزي - دفتر الحسابات")
            .setMessage("نسخة محلية تعمل بقاعدة SQLite.\nبدون حد 250 عملية.\nالهاتف: ${db.setting("phone", "776425052")}")
            .setPositiveButton("حسناً", null)
            .show()
    }

    private fun confirmDelete(message: String, yes: () -> Unit) {
        AlertDialog.Builder(this).setMessage(message).setPositiveButton("حذف") { _, _ -> yes() }.setNegativeButton("إلغاء", null).show()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun EditText.num(): Double = text.toString().toDoubleOrNull() ?: 0.0
}
