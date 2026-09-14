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

    suspend fun saveExpense(expense: Expense) = withContext(Dispatchers.IO) { dao.insertExpense(expense) }
    
    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) { dao.deleteExpense(expense) }

    suspend fun saveInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) = withContext(Dispatchers.IO) {
        val id = dao.insertInvoice(invoice)
        items.forEach { dao.insertInvoiceItem(it.copy(invoiceId = id.toInt())) }
    }
    
    suspend fun deleteInvoice(invoice: Invoice) = withContext(Dispatchers.IO) { dao.deleteInvoice(invoice) }
    
    fun getInvoiceItems(invoiceId: Int) = dao.getInvoiceItems(invoiceId)
    
    suspend fun saveSettings(s: AppSettings) = withContext(Dispatchers.IO) { dao.insertSettings(s) }
}
