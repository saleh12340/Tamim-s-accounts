package com.aistudio.tamimsaccounts

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AccountsRepository(private val dao: AccountsDao) {
    val customers: Flow<List<Customer>> = dao.getAllCustomers()
    val transactions: Flow<List<Tx>> = dao.getAllTransactions()
    val products: Flow<List<Product>> = dao.getAllProducts()
    val expenses: Flow<List<Expense>> = dao.getAllExpenses()
    val invoices: Flow<List<Invoice>> = dao.getAllInvoices()
    val suppliers: Flow<List<Supplier>> = dao.getAllSuppliers()
    val settings: Flow<AppSettings?> = dao.getSettings()

    suspend fun saveCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        if (customer.id == 0) dao.insertCustomer(customer)
        else dao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) { dao.deleteCustomer(customer) }

    suspend fun saveTransaction(tx: Tx) = withContext(Dispatchers.IO) { dao.insertTransaction(tx) }

    fun getCustomerTransactions(customerId: Int) = dao.getTransactionsForCustomer(customerId)

    suspend fun deleteTransaction(tx: Tx) = withContext(Dispatchers.IO) { dao.deleteTransaction(tx) }

    suspend fun saveProduct(product: Product) = withContext(Dispatchers.IO) {
        if (product.id == 0) dao.insertProduct(product)
        else dao.updateProduct(product)
    }
    
    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) { dao.deleteProduct(product) }

    suspend fun saveExpense(expense: Expense) = withContext(Dispatchers.IO) {
        if (expense.id == 0) dao.insertExpense(expense)
        else dao.insertExpense(expense)
    }
    
    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) { dao.deleteExpense(expense) }

    suspend fun saveInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) = withContext(Dispatchers.IO) {
        val id = dao.insertInvoice(invoice)
        items.forEach { dao.insertInvoiceItem(it.copy(invoiceId = id.toInt())) }
    }
    
    suspend fun deleteInvoice(invoice: Invoice) = withContext(Dispatchers.IO) { dao.deleteInvoice(invoice) }
    
    fun getInvoiceItems(invoiceId: Int) = dao.getInvoiceItems(invoiceId)

    suspend fun saveSupplier(supplier: Supplier) = withContext(Dispatchers.IO) {
        if (supplier.id == 0) dao.insertSupplier(supplier)
        else dao.updateSupplier(supplier)
    }

    suspend fun deleteSupplier(supplier: Supplier) = withContext(Dispatchers.IO) { dao.deleteSupplier(supplier) }
    
    suspend fun saveSettings(s: AppSettings) = withContext(Dispatchers.IO) { dao.insertSettings(s) }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.deleteAllTransactions()
        dao.deleteAllInvoiceItems()
        dao.deleteAllInvoices()
        dao.deleteAllExpenses()
        dao.deleteAllProducts()
        dao.deleteAllCustomers()
    }

    suspend fun exportDatabaseJson(): String = withContext(Dispatchers.IO) {
        val root = org.json.JSONObject()
        root.put("version", 1)
        root.put("exportedAt", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date()))

        // Customers
        val customersArr = org.json.JSONArray()
        dao.getAllCustomersList().forEach { c ->
            val cObj = org.json.JSONObject()
            cObj.put("id", c.id)
            cObj.put("name", c.name)
            cObj.put("phone", c.phone)
            cObj.put("balance", c.balance)
            cObj.put("notes", c.notes)
            customersArr.put(cObj)
        }
        root.put("customers", customersArr)

        // Transactions
        val txsArr = org.json.JSONArray()
        dao.getAllTransactionsList().forEach { t ->
            val tObj = org.json.JSONObject()
            tObj.put("id", t.id)
            tObj.put("customerId", t.customerId)
            tObj.put("type", t.type.name)
            tObj.put("amount", t.amount)
            tObj.put("currency", t.currency)
            tObj.put("date", t.date)
            tObj.put("note", t.note)
            txsArr.put(tObj)
        }
        root.put("transactions", txsArr)

        // Products
        val productsArr = org.json.JSONArray()
        dao.getAllProductsList().forEach { p ->
            val pObj = org.json.JSONObject()
            pObj.put("id", p.id)
            pObj.put("name", p.name)
            pObj.put("barcode", p.barcode)
            pObj.put("buyPrice", p.buyPrice)
            pObj.put("sellPrice", p.sellPrice)
            pObj.put("stock", p.stock)
            pObj.put("minStock", p.minStock)
            pObj.put("notes", p.notes)
            productsArr.put(pObj)
        }
        root.put("products", productsArr)

        // Expenses
        val expensesArr = org.json.JSONArray()
        dao.getAllExpensesList().forEach { e ->
            val eObj = org.json.JSONObject()
            eObj.put("id", e.id)
            eObj.put("title", e.title)
            eObj.put("amount", e.amount)
            eObj.put("currency", e.currency)
            eObj.put("date", e.date)
            eObj.put("note", e.note)
            expensesArr.put(eObj)
        }
        root.put("expenses", expensesArr)

        // Invoices
        val invoicesArr = org.json.JSONArray()
        dao.getAllInvoicesList().forEach { inv ->
            val invObj = org.json.JSONObject()
            invObj.put("id", inv.id)
            invObj.put("type", inv.type.name)
            invObj.put("customerId", inv.customerId ?: org.json.JSONObject.NULL)
            invObj.put("customerOrSupplierName", inv.customerOrSupplierName ?: "")
            invObj.put("date", inv.date)
            invObj.put("total", inv.total)
            invObj.put("paid", inv.paid)
            invObj.put("note", inv.note)
            invoicesArr.put(invObj)
        }
        root.put("invoices", invoicesArr)

        // Invoice items
        val itemsArr = org.json.JSONArray()
        dao.getAllInvoiceItemsList().forEach { itm ->
            val itmObj = org.json.JSONObject()
            itmObj.put("id", itm.id)
            itmObj.put("invoiceId", itm.invoiceId)
            itmObj.put("itemName", itm.itemName)
            itmObj.put("quantity", itm.quantity)
            itmObj.put("unitPrice", itm.unitPrice)
            itmObj.put("total", itm.total)
            itemsArr.put(itmObj)
        }
        root.put("invoiceItems", itemsArr)

        root.toString(2)
    }

    suspend fun restoreDatabaseFromJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trimmed = jsonString.trim()
            var restoredCustomers = 0
            var restoredTxs = 0
            var restoredProducts = 0
            var restoredExpenses = 0
            var restoredInvoices = 0

            // Map old customer id to new customer id if needed
            val customerIdMap = mutableMapOf<Int, Int>()

            if (trimmed.startsWith("[")) {
                // Array of customers (e.g. from web simple export)
                val arr = org.json.JSONArray(trimmed)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val name = obj.optString("name", obj.optString("customerName", "عميل"))
                    val phone = obj.optString("phone", obj.optString("phoneNumber", ""))
                    val balance = obj.optDouble("balance", obj.optDouble("debt", 0.0))
                    val notes = obj.optString("notes", "")
                    dao.insertCustomer(Customer(name = name, phone = phone, balance = balance, notes = notes))
                    restoredCustomers++
                }
            } else {
                val root = org.json.JSONObject(trimmed)

                // 1. Settings (if present)
                if (root.has("settings")) {
                    val sObj = root.optJSONObject("settings")
                    if (sObj != null) {
                        val storeName = sObj.optString("storeName", "بقالة العزي")
                        val phone = sObj.optString("phone", "777000000")
                        val currency = sObj.optString("currency", "ريال")
                        dao.insertSettings(AppSettings(id = 1, storeName = storeName, phone = phone, currency = currency))
                    }
                }

                // 2. Customers
                val custArr = root.optJSONArray("customers") ?: root.optJSONArray("accounts") ?: root.optJSONArray("clients")
                if (custArr != null) {
                    for (i in 0 until custArr.length()) {
                        val obj = custArr.getJSONObject(i)
                        val oldId = obj.optInt("id", 0)
                        val name = obj.optString("name", obj.optString("customerName", "عميل"))
                        val phone = obj.optString("phone", obj.optString("phoneNumber", ""))
                        val balance = obj.optDouble("balance", obj.optDouble("debt", 0.0))
                        val notes = obj.optString("notes", "")
                        
                        val newId = dao.insertCustomer(Customer(name = name, phone = phone, balance = balance, notes = notes)).toInt()
                        if (oldId != 0) {
                            customerIdMap[oldId] = newId
                        }
                        restoredCustomers++
                    }
                }

                // 3. Transactions
                val txArr = root.optJSONArray("transactions") ?: root.optJSONArray("txs") ?: root.optJSONArray("operations")
                if (txArr != null) {
                    for (i in 0 until txArr.length()) {
                        val obj = txArr.getJSONObject(i)
                        val oldCustId = obj.optInt("customerId", 0)
                        val custId = customerIdMap[oldCustId] ?: oldCustId
                        val typeStr = obj.optString("type", "DEBIT").uppercase()
                        val type = if (typeStr == "CREDIT" || typeStr == "PAYMENT") TransactionType.CREDIT else TransactionType.DEBIT
                        val amount = obj.optDouble("amount", 0.0)
                        val currency = obj.optString("currency", "ريال")
                        val date = obj.optString("date", "")
                        val note = obj.optString("note", obj.optString("details", ""))

                        if (amount > 0) {
                            dao.insertTransaction(Tx(
                                customerId = custId,
                                type = type,
                                amount = amount,
                                currency = currency,
                                date = date,
                                note = note
                            ))
                            restoredTxs++
                        }
                    }
                }

                // 4. Products
                val prodArr = root.optJSONArray("products") ?: root.optJSONArray("items") ?: root.optJSONArray("inventory")
                if (prodArr != null) {
                    for (i in 0 until prodArr.length()) {
                        val obj = prodArr.getJSONObject(i)
                        val name = obj.optString("name", obj.optString("productName", ""))
                        val barcode = obj.optString("barcode", "")
                        val buyPrice = obj.optDouble("buyPrice", 0.0)
                        val sellPrice = obj.optDouble("sellPrice", obj.optDouble("price", 0.0))
                        val stock = obj.optInt("stock", obj.optInt("quantity", 0))
                        val minStock = obj.optInt("minStock", 5)
                        val notes = obj.optString("notes", "")

                        if (name.isNotBlank()) {
                            dao.insertProduct(Product(
                                name = name,
                                barcode = barcode,
                                buyPrice = buyPrice,
                                sellPrice = sellPrice,
                                stock = stock,
                                minStock = minStock,
                                notes = notes
                            ))
                            restoredProducts++
                        }
                    }
                }

                // 5. Expenses
                val expArr = root.optJSONArray("expenses") ?: root.optJSONArray("costs")
                if (expArr != null) {
                    for (i in 0 until expArr.length()) {
                        val obj = expArr.getJSONObject(i)
                        val title = obj.optString("title", obj.optString("category", "مصروف"))
                        val amount = obj.optDouble("amount", 0.0)
                        val currency = obj.optString("currency", "ريال")
                        val date = obj.optString("date", "")
                        val note = obj.optString("note", "")

                        if (amount > 0) {
                            dao.insertExpense(Expense(
                                title = title,
                                amount = amount,
                                currency = currency,
                                date = date,
                                note = note
                            ))
                            restoredExpenses++
                        }
                    }
                }

                // 6. Invoices
                val invArr = root.optJSONArray("invoices") ?: root.optJSONArray("bills")
                if (invArr != null) {
                    for (i in 0 until invArr.length()) {
                        val obj = invArr.getJSONObject(i)
                        val typeStr = obj.optString("type", "SALE").uppercase()
                        val type = if (typeStr == "PURCHASE") InvoiceType.PURCHASE else InvoiceType.SALE
                        val oldCustId = if (obj.has("customerId")) obj.optInt("customerId") else null
                        val custId = if (oldCustId != null) (customerIdMap[oldCustId] ?: oldCustId) else null
                        val custName = obj.optString("customerOrSupplierName", "")
                        val date = obj.optString("date", "")
                        val total = obj.optDouble("total", 0.0)
                        val paid = obj.optDouble("paid", 0.0)
                        val note = obj.optString("note", "")

                        dao.insertInvoice(Invoice(
                            type = type,
                            customerId = custId,
                            customerOrSupplierName = custName,
                            date = date,
                            total = total,
                            paid = paid,
                            note = note
                        ))
                        restoredInvoices++
                    }
                }
            }

            val summary = "تمت الاستعادة بنجاح: $restoredCustomers عميل، $restoredTxs حركة، $restoredProducts صنف مخزون، $restoredExpenses مصروف، $restoredInvoices فاتورة"
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val tempFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}.db")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("تعذر قراءة الملف المختار"))

            // Check if file is SQLite format (starts with "SQLite format 3")
            val isSqlite = try {
                val header = ByteArray(16)
                tempFile.inputStream().use { it.read(header) }
                String(header).startsWith("SQLite format 3")
            } catch (e: Exception) {
                false
            }

            val result = if (isSqlite) {
                restoreFromSqliteDatabase(tempFile)
            } else {
                val jsonString = tempFile.readText()
                restoreDatabaseFromJson(jsonString)
            }

            tempFile.delete()
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromSqliteDatabase(dbFile: File): Result<String> = withContext(Dispatchers.IO) {
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)

            // 1. Read Groups
            val groupsMap = mutableMapOf<Int, String>()
            try {
                val cursor = db.rawQuery("SELECT ID, name FROM groups", null)
                while (cursor.moveToNext()) {
                    val gId = cursor.getInt(0)
                    val gName = cursor.getString(1) ?: ""
                    groupsMap[gId] = gName
                }
                cursor.close()
            } catch (e: Exception) {
                // Table might not exist
            }

            // 2. Read Currency
            val currencyMap = mutableMapOf<Int, String>()
            try {
                val cursor = db.rawQuery("SELECT ID, name FROM currency", null)
                while (cursor.moveToNext()) {
                    val cId = cursor.getInt(0)
                    val cName = cursor.getString(1) ?: ""
                    currencyMap[cId] = cName
                }
                cursor.close()
            } catch (e: Exception) {
                // Table might not exist
            }

            // Clean existing transactions & customers
            dao.deleteAllTransactions()
            dao.deleteAllCustomers()

            // 3. Read Customers
            var custCount = 0
            val oldToNewCustId = mutableMapOf<Int, Int>()
            val custCursor = db.rawQuery("SELECT ID, name, gsm, g_id, param1 FROM customers", null)
            while (custCursor.moveToNext()) {
                val oldId = custCursor.getInt(0)
                val name = custCursor.getString(1) ?: "عميل $oldId"
                val phone = custCursor.getString(2) ?: ""
                val gId = custCursor.getInt(3)
                val param1 = custCursor.getString(4) ?: ""

                val groupName = groupsMap[gId]
                val noteWithGroup = if (!groupName.isNullOrBlank()) {
                    if (param1.isNotBlank()) "$param1 ($groupName)" else groupName
                } else param1

                val newId = dao.insertCustomer(Customer(
                    id = 0,
                    name = name.trim(),
                    phone = phone.trim(),
                    balance = 0.0,
                    notes = noteWithGroup
                )).toInt()
                oldToNewCustId[oldId] = newId
                custCount++
            }
            custCursor.close()

            // 4. Read Transactions & calculate balances
            var txCount = 0
            val txCursor = db.rawQuery("SELECT ID, cus_id, [in], out, date_, remarks, param2, curr_id FROM transactions", null)
            val customerBalances = mutableMapOf<Int, Double>()

            while (txCursor.moveToNext()) {
                val oldCusId = txCursor.getString(1)?.toIntOrNull() ?: 0
                val newCusId = oldToNewCustId[oldCusId] ?: oldCusId
                val inVal = txCursor.getString(2) ?: "1"
                val outVal = txCursor.getDouble(3)
                val dateStr = txCursor.getString(4) ?: ""
                val remarks = txCursor.getString(5) ?: ""
                val timeStr = txCursor.getString(6) ?: ""
                val currId = txCursor.getInt(7)
                val currName = currencyMap[currId] ?: "ريال"

                // In this database format:
                // [in] == "1": DEBIT (عليه / أخذ بضاعة)
                // [in] == "-1" or "0": CREDIT (له / سداد كاش)
                val type = if (inVal == "-1" || inVal == "0") TransactionType.CREDIT else TransactionType.DEBIT
                val fullDate = if (timeStr.isNotBlank()) "$dateStr $timeStr" else dateStr

                if (outVal > 0 && newCusId > 0) {
                    dao.insertTransaction(Tx(
                        customerId = newCusId,
                        type = type,
                        amount = outVal,
                        currency = currName,
                        date = fullDate,
                        note = remarks
                    ))

                    val currentBal = customerBalances[newCusId] ?: 0.0
                    val diff = if (type == TransactionType.DEBIT) outVal else -outVal
                    customerBalances[newCusId] = currentBal + diff
                    txCount++
                }
            }
            txCursor.close()

            // 5. Update customer balances
            customerBalances.forEach { (cId, bal) ->
                dao.updateCustomerBalance(cId, bal)
            }

            Result.success("تمت استعادة قاعدة البيانات بنجاح!\nالعملاء: $custCount عميل\nالحركات: $txCount حركة ومستند")
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            db?.close()
        }
    }

    suspend fun loadSampleData() = withContext(Dispatchers.IO) {
        // Sample Settings
        dao.insertSettings(AppSettings(id = 1, storeName = "بقالة العزي النموذجية", phone = "777123456", currency = "ريال"))

        // Sample Customers
        val c1Id = dao.insertCustomer(Customer(name = "أحمد محمد الحاشدي", phone = "771234567", balance = 14500.0, notes = "عميل شهري موثوق")).toInt()
        val c2Id = dao.insertCustomer(Customer(name = "خالد عبدالله الصبري", phone = "733987654", balance = 8200.0, notes = "يسدد كل جمعة")).toInt()
        val c3Id = dao.insertCustomer(Customer(name = "سعيد قائد الحكيمي", phone = "711554433", balance = -2000.0, notes = "له دفعة مقدمة")).toInt()
        val c4Id = dao.insertCustomer(Customer(name = "ياسر عمر العديني", phone = "770112233", balance = 0.0, notes = "حساب خالص")).toInt()

        // Sample Transactions
        dao.insertTransaction(Tx(customerId = c1Id, type = TransactionType.DEBIT, amount = 9500.0, currency = "ريال", date = "2026-09-12 10:30", note = "أكياس سكر وأرز وزيت"))
        dao.insertTransaction(Tx(customerId = c1Id, type = TransactionType.DEBIT, amount = 5000.0, currency = "ريال", date = "2026-09-13 18:45", note = "حليب معلبات وبسكويت"))
        dao.insertTransaction(Tx(customerId = c2Id, type = TransactionType.DEBIT, amount = 12000.0, currency = "ريال", date = "2026-09-10 14:00", note = "مشتريات بقالة عامة"))
        dao.insertTransaction(Tx(customerId = c2Id, type = TransactionType.CREDIT, amount = 3800.0, currency = "ريال", date = "2026-09-14 09:15", note = "دفعة سداد كاش"))

        // Sample Products (Inventory)
        dao.insertProduct(Product(name = "أرز بسمتي الشعلان 10 كجم", barcode = "628100112233", buyPrice = 8500.0, sellPrice = 9800.0, stock = 18, minStock = 5, notes = "مطلوب بكثرة"))
        dao.insertProduct(Product(name = "سكر الأسرة 5 كجم", barcode = "628100223344", buyPrice = 3200.0, sellPrice = 3700.0, stock = 4, minStock = 10, notes = "قارب على النفاد"))
        dao.insertProduct(Product(name = "زيت عافية ذرة 1.5 لتر", barcode = "628100334455", buyPrice = 2400.0, sellPrice = 2850.0, stock = 25, minStock = 8, notes = "كرتون 12 حبة"))
        dao.insertProduct(Product(name = "حليب نيدو مجفف 900 جرام", barcode = "761303212345", buyPrice = 4100.0, sellPrice = 4800.0, stock = 3, minStock = 6, notes = "يحتاج طلب من المورد"))
        dao.insertProduct(Product(name = "تونة ريم درجة أولى", barcode = "628100445566", buyPrice = 650.0, sellPrice = 800.0, stock = 45, minStock = 15, notes = "تاريخ حديث"))

        // Sample Expenses
        dao.insertExpense(Expense(title = "فاتورة الكهرباء لشهر أغسطس", amount = 6500.0, currency = "ريال", date = "2026-09-05", note = "سداد نقدي"))
        dao.insertExpense(Expense(title = "أجرة نقل كراتين بضاعة", amount = 3000.0, currency = "ريال", date = "2026-09-08", note = "نقل من سوق الجملة"))
        dao.insertExpense(Expense(title = "أكياس ومطبوعات للمحل", amount = 1500.0, currency = "ريال", date = "2026-09-11", note = "شراء أكياس تغليف"))

        // Sample Invoices
        val inv1Id = dao.insertInvoice(Invoice(type = InvoiceType.SALE, customerId = c1Id, customerOrSupplierName = "أحمد محمد الحاشدي", date = "2026-09-12 10:30", total = 9500.0, paid = 0.0, note = "فاتورة آجلة")).toInt()
        dao.insertInvoiceItem(InvoiceItem(invoiceId = inv1Id, itemName = "أرز بسمتي الشعلان 10 كجم", quantity = 1, unitPrice = 9800.0, total = 9800.0))

        val inv2Id = dao.insertInvoice(Invoice(type = InvoiceType.SALE, customerId = null, customerOrSupplierName = "زبون نقدي (كاش)", date = "2026-09-14 08:30", total = 3650.0, paid = 3650.0, note = "مبيعات نقدية صباحية")).toInt()
        dao.insertInvoiceItem(InvoiceItem(invoiceId = inv2Id, itemName = "زيت عافية ذرة 1.5 لتر", quantity = 1, unitPrice = 2850.0, total = 2850.0))
        dao.insertInvoiceItem(InvoiceItem(invoiceId = inv2Id, itemName = "تونة ريم درجة أولى", quantity = 1, unitPrice = 800.0, total = 800.0))
    }
}
