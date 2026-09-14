package com.aistudio.tamimsaccounts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

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
