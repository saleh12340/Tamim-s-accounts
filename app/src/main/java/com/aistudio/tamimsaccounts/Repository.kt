package com.aistudio.tamimsaccounts

import kotlinx.coroutines.flow.Flow

class AccountsRepository(private val dao: AccountsDao) {
    val customers: Flow<List<Customer>> = dao.getAllCustomers()
    val transactions: Flow<List<Tx>> = dao.getAllTransactions()
    val products: Flow<List<Product>> = dao.getAllProducts()
    val expenses: Flow<List<Expense>> = dao.getAllExpenses()
    val invoices: Flow<List<Invoice>> = dao.getAllInvoices()
    val settings: Flow<AppSettings?> = dao.getSettings()

    suspend fun saveCustomer(customer: Customer) {
        if (customer.id == 0) dao.insertCustomer(customer)
        else dao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) = dao.deleteCustomer(customer)

    suspend fun saveTransaction(tx: Tx) {
        dao.insertTransaction(tx)
        // Also update customer balance? In the react app, does saving tx update balance automatically?
        // Wait, the react app computes balance on the fly, or updates it? We'll see.
    }

    fun getCustomerTransactions(customerId: Int) = dao.getTransactionsForCustomer(customerId)

    suspend fun deleteTransaction(tx: Tx) = dao.deleteTransaction(tx)

    suspend fun saveProduct(product: Product) {
        if (product.id == 0) dao.insertProduct(product)
        else dao.updateProduct(product)
    }
    
    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)

    suspend fun saveExpense(expense: Expense) = dao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    suspend fun saveInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) {
        val id = dao.insertInvoice(invoice)
        items.forEach { dao.insertInvoiceItem(it.copy(invoiceId = id.toInt())) }
    }
    
    suspend fun deleteInvoice(invoice: Invoice) = dao.deleteInvoice(invoice)
    fun getInvoiceItems(invoiceId: Int) = dao.getInvoiceItems(invoiceId)
    
    suspend fun saveSettings(s: AppSettings) = dao.insertSettings(s)
}
