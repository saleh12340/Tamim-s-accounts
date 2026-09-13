package com.saleh.tamimaccounts

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var list: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(this)
        showAccounts()
    }

    private fun base(title: String): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(20, 20, 20, 12)
        }
        val header = TextView(this).apply {
            text = "$title\nهاتف: 776425052"
            textSize = 22f
            setPadding(0, 0, 0, 16)
        }
        root.addView(header)

        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply {
            addView(list)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        })

        val nav = LinearLayout(this).apply { gravity = Gravity.CENTER }
        listOf("الإعدادات", "الحسابات", "العمليات").forEachIndexed { index, label ->
            val button = Button(this).apply {
                text = label
                setOnClickListener {
                    if (index == 1) showAccounts()
                    else Toast.makeText(this@MainActivity, label, Toast.LENGTH_SHORT).show()
                }
            }
            nav.addView(button, LinearLayout.LayoutParams(0, 60, 1f))
        }
        root.addView(nav)
        return root
    }

    private fun showAccounts() {
        setContentView(base("بقالة العزي - دفتر الحسابات"))
        list.addView(Button(this).apply {
            text = "+ إضافة حساب"
            setOnClickListener { addCustomer() }
        })
        db.customers().forEach { customer ->
            val view = TextView(this).apply {
                text = "${customer.name}\n${customer.phone}   الرصيد: ${customer.balance}"
                textSize = 18f
                setPadding(14, 18, 14, 18)
                setOnClickListener { addTransaction(customer.id, customer.name) }
            }
            list.addView(view)
        }
    }

    private fun addCustomer() {
        val name = EditText(this).apply { hint = "اسم العميل" }
        val phone = EditText(this).apply {
            hint = "رقم الهاتف"
            inputType = InputType.TYPE_CLASS_PHONE
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(name)
            addView(phone)
        }
        AlertDialog.Builder(this)
            .setTitle("إضافة حساب")
            .setView(box)
            .setPositiveButton("حفظ") { _, _ ->
                if (name.text.isNotBlank()) {
                    db.addCustomer(name.text.toString(), phone.text.toString())
                    showAccounts()
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun addTransaction(id: Long, name: String) {
        val amount = EditText(this).apply {
            hint = "المبلغ"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val note = EditText(this).apply { hint = "البيان" }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(amount)
            addView(note)
        }
        AlertDialog.Builder(this)
            .setTitle("عملية لـ $name")
            .setView(box)
            .setPositiveButton("عليه") { _, _ ->
                saveTransaction(id, "DEBIT", amount.text.toString(), note.text.toString())
            }
            .setNeutralButton("له") { _, _ ->
                saveTransaction(id, "CREDIT", amount.text.toString(), note.text.toString())
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun saveTransaction(id: Long, type: String, amountText: String, note: String) {
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "أدخل مبلغاً صحيحاً", Toast.LENGTH_SHORT).show()
            return
        }
        db.addTransaction(id, type, amount, note)
        showAccounts()
    }
}
