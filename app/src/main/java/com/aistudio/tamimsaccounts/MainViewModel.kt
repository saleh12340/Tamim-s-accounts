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
    fun deleteCustomer(customer: Customer) = viewModelScope.launch { repository.deleteCustomer(customer) }

    fun saveTransaction(tx: Tx, customer: Customer) = viewModelScope.launch {
        repository.saveTransaction(tx)
        // Update customer balance: DEBIT = customer owes more (+), CREDIT = customer pays (-)
        val diff = if (tx.type == TransactionType.DEBIT) tx.amount else -tx.amount
        val updatedBalance = customer.balance + diff
        repository.saveCustomer(customer.copy(balance = updatedBalance))
    }
    
    fun deleteTransaction(tx: Tx, customer: Customer) = viewModelScope.launch {
        repository.deleteTransaction(tx)
        // Revert balance
        val diff = if (tx.type == TransactionType.DEBIT) -tx.amount else tx.amount
        val updatedBalance = customer.balance + diff
        repository.saveCustomer(customer.copy(balance = updatedBalance))
    }

    fun saveProduct(product: Product) = viewModelScope.launch { repository.saveProduct(product) }
    fun deleteProduct(product: Product) = viewModelScope.launch { repository.deleteProduct(product) }

    fun adjustProductStock(product: Product, delta: Int) = viewModelScope.launch {
        val newStock = (product.stock + delta).coerceAtLeast(0)
        repository.saveProduct(product.copy(stock = newStock))
    }

    fun saveExpense(expense: Expense) = viewModelScope.launch { repository.saveExpense(expense) }
    fun deleteExpense(expense: Expense) = viewModelScope.launch { repository.deleteExpense(expense) }

    fun saveInvoiceWithItems(
        invoice: Invoice, 
        items: List<InvoiceItem>,
        updateStockAndCustomer: Boolean = true
    ) = viewModelScope.launch {
        repository.saveInvoiceWithItems(invoice, items)
        
        if (updateStockAndCustomer) {
            // Deduct stock if sale, or increase if purchase
            val currentProducts = products.value
            items.forEach { item ->
                val matchingProduct = currentProducts.find { it.name.trim().equals(item.itemName.trim(), ignoreCase = true) }
                if (matchingProduct != null) {
                    val delta = if (invoice.type == InvoiceType.SALE) -item.quantity else item.quantity
                    val updatedStock = (matchingProduct.stock + delta).coerceAtLeast(0)
                    repository.saveProduct(matchingProduct.copy(stock = updatedStock))
                }
            }
            
            // If sale invoice is tied to customer and has unpaid balance, add DEBIT transaction
            if (invoice.type == InvoiceType.SALE && invoice.customerId != null) {
                val customer = customers.value.find { it.id == invoice.customerId }
                val remainingUnpaid = invoice.total - invoice.paid
                if (customer != null && remainingUnpaid > 0) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                    val dateStr = sdf.format(Date())
                    val tx = Tx(
                        customerId = customer.id,
                        type = TransactionType.DEBIT,
                        amount = remainingUnpaid,
                        currency = "ريال",
                        date = dateStr,
                        note = "متبقي فاتورة بيع #${invoice.id}"
                    )
                    saveTransaction(tx, customer)
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

    fun exportBackup(onResult: (String) -> Unit) = viewModelScope.launch {
        val json = repository.exportDatabaseJson()
        onResult(json)
    }

    fun restoreBackup(jsonString: String, onComplete: (Result<String>) -> Unit) = viewModelScope.launch {
        val res = repository.restoreDatabaseFromJson(jsonString)
        onComplete(res)
    }
}
