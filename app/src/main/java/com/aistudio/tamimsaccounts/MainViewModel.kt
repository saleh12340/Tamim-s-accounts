package com.aistudio.tamimsaccounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(private val repository: AccountsRepository) : ViewModel() {
    val customers = repository.customers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val products = repository.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = repository.expenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val invoices = repository.invoices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suppliers = repository.suppliers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveCustomer(customer: Customer) = viewModelScope.launch { repository.saveCustomer(customer) }

    fun deleteCustomer(customer: Customer) = viewModelScope.launch {
        // Transactions currently have no FK cascade. Remove them explicitly first
        // so deleting an account cannot leave orphaned ledger rows.
        transactions.value.filter { it.customerId == customer.id }.forEach { repository.deleteTransaction(it) }
        repository.deleteCustomer(customer)
    }

    fun saveTransaction(tx: Tx, customer: Customer) = viewModelScope.launch {
        repository.saveTransaction(tx)
        val diff = if (tx.type == TransactionType.DEBIT) tx.amount else -tx.amount
        repository.saveCustomer(customer.copy(balance = customer.balance + diff))
    }

    fun deleteTransaction(tx: Tx, customer: Customer) = viewModelScope.launch {
        repository.deleteTransaction(tx)
        val diff = if (tx.type == TransactionType.DEBIT) -tx.amount else tx.amount
        repository.saveCustomer(customer.copy(balance = customer.balance + diff))
    }

    fun saveProduct(product: Product) = viewModelScope.launch { repository.saveProduct(product) }
    fun deleteProduct(product: Product) = viewModelScope.launch { repository.deleteProduct(product) }

    fun adjustProductStock(product: Product, delta: Int) = viewModelScope.launch {
        repository.saveProduct(product.copy(stock = (product.stock + delta).coerceAtLeast(0)))
    }

    fun saveExpense(expense: Expense) = viewModelScope.launch { repository.saveExpense(expense) }
    fun deleteExpense(expense: Expense) = viewModelScope.launch { repository.deleteExpense(expense) }

    fun saveInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>, updateStockAndCustomer: Boolean = true) = viewModelScope.launch {
        repository.saveInvoiceWithItems(invoice, items)
        if (updateStockAndCustomer) {
            val currentProducts = products.value
            items.forEach { item ->
                currentProducts.find { it.name.trim().equals(item.itemName.trim(), ignoreCase = true) }?.let { product ->
                    val delta = if (invoice.type == InvoiceType.SALE) -item.quantity else item.quantity
                    repository.saveProduct(product.copy(stock = (product.stock + delta).coerceAtLeast(0)))
                }
            }
            if (invoice.type == InvoiceType.SALE && invoice.customerId != null) {
                val customer = customers.value.find { it.id == invoice.customerId }
                val remaining = invoice.total - invoice.paid
                if (customer != null && remaining > 0) {
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                    saveTransaction(Tx(customer.id, TransactionType.DEBIT, remaining, "ريال", date, "متبقي فاتورة بيع"), customer)
                }
            }
        }
    }

    fun deleteInvoice(invoice: Invoice) = viewModelScope.launch { repository.deleteInvoice(invoice) }
    fun getInvoiceItems(invoiceId: Int) = repository.getInvoiceItems(invoiceId)
    fun saveSupplier(supplier: Supplier) = viewModelScope.launch { repository.saveSupplier(supplier) }
    fun deleteSupplier(supplier: Supplier) = viewModelScope.launch { repository.deleteSupplier(supplier) }
    fun saveSettings(s: AppSettings) = viewModelScope.launch { repository.saveSettings(s) }
    fun loadSampleData() = viewModelScope.launch { repository.loadSampleData() }
    fun clearAllData() = viewModelScope.launch { repository.clearAllData() }

    fun exportBackup(onResult: (String) -> Unit) = viewModelScope.launch { onResult(repository.exportDatabaseJson()) }
    fun restoreBackup(jsonString: String, onComplete: (Result<String>) -> Unit) = viewModelScope.launch { onComplete(repository.restoreDatabaseFromJson(jsonString)) }
    fun restoreFromUri(context: android.content.Context, uri: android.net.Uri, onComplete: (Result<String>) -> Unit) = viewModelScope.launch { onComplete(repository.restoreFromUri(context, uri)) }
}
