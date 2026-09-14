package com.aistudio.tamimsaccounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AccountsRepository) : ViewModel() {
    val customers = repository.customers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val products = repository.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = repository.expenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val invoices = repository.invoices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveCustomer(customer: Customer) = viewModelScope.launch { repository.saveCustomer(customer) }
    fun deleteCustomer(customer: Customer) = viewModelScope.launch { repository.deleteCustomer(customer) }

    fun saveTransaction(tx: Tx, customer: Customer) = viewModelScope.launch {
        repository.saveTransaction(tx)
        // Update customer balance locally
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

    fun saveExpense(expense: Expense) = viewModelScope.launch { repository.saveExpense(expense) }
    fun deleteExpense(expense: Expense) = viewModelScope.launch { repository.deleteExpense(expense) }

    fun saveInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) = viewModelScope.launch {
        repository.saveInvoiceWithItems(invoice, items)
    }
    fun deleteInvoice(invoice: Invoice) = viewModelScope.launch { repository.deleteInvoice(invoice) }

    fun saveSettings(s: AppSettings) = viewModelScope.launch { repository.saveSettings(s) }
}
