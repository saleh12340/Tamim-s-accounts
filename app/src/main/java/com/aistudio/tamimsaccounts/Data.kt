package com.aistudio.tamimsaccounts

import androidx.room.*
import kotlinx.coroutines.flow.Flow


enum class TransactionType { DEBIT, CREDIT }
enum class InvoiceType { SALE, PURCHASE }

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val balance: Double = 0.0,
    val groupId: Int? = null,
    val notes: String
)

@Entity(tableName = "transactions")
data class Tx(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val type: TransactionType,
    val amount: Double,
    val currency: String,
    val date: String,
    val note: String,
    val shareRef: String? = null
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val barcode: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val stock: Int,
    val minStock: Int,
    val notes: String
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val currency: String,
    val date: String,
    val note: String
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: InvoiceType,
    val customerId: Int? = null,
    val supplierId: Int? = null,
    val customerOrSupplierName: String?,
    val date: String,
    val total: Double,
    val paid: Double,
    val note: String
)

@Entity(tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(entity = Invoice::class, parentColumns = ["id"], childColumns = ["invoiceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Int,
    val itemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val balance: Double,
    val notes: String
)

@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val storeName: String = "بقالة العزي",
    val phone: String = "777000000",
    val currency: String = "ريال"
)

@Dao
interface AccountsDao {
    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers")
    fun getAllCustomersList(): List<Customer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomer(customer: Customer): Long

    @Update
    fun updateCustomer(customer: Customer): Int

    @Delete
    fun deleteCustomer(customer: Customer): Int

    @Query("UPDATE customers SET balance = :newBalance WHERE id = :customerId")
    fun updateCustomerBalance(customerId: Int, newBalance: Double): Int

    @Query("DELETE FROM customers")
    fun deleteAllCustomers(): Int

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Tx>>

    @Query("SELECT * FROM transactions")
    fun getAllTransactionsList(): List<Tx>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC")
    fun getTransactionsForCustomer(customerId: Int): Flow<List<Tx>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(tx: Tx): Long

    @Delete
    fun deleteTransaction(tx: Tx): Int

    @Query("DELETE FROM transactions")
    fun deleteAllTransactions(): Int

    // Products
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products")
    fun getAllProductsList(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: Product): Long

    @Update
    fun updateProduct(product: Product): Int

    @Delete
    fun deleteProduct(product: Product): Int

    @Query("DELETE FROM products")
    fun deleteAllProducts(): Int

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses")
    fun getAllExpensesList(): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExpense(expense: Expense): Long
    
    @Delete
    fun deleteExpense(expense: Expense): Int

    @Query("DELETE FROM expenses")
    fun deleteAllExpenses(): Int

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices")
    fun getAllInvoicesList(): List<Invoice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInvoice(invoice: Invoice): Long

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getInvoiceItems(invoiceId: Int): Flow<List<InvoiceItem>>

    @Query("SELECT * FROM invoice_items")
    fun getAllInvoiceItemsList(): List<InvoiceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInvoiceItem(item: InvoiceItem): Long
    
    @Delete
    fun deleteInvoice(invoice: Invoice): Int

    @Query("DELETE FROM invoices")
    fun deleteAllInvoices(): Int

    @Query("DELETE FROM invoice_items")
    fun deleteAllInvoiceItems(): Int

    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSupplier(supplier: Supplier): Long

    @Update
    fun updateSupplier(supplier: Supplier): Int

    @Delete
    fun deleteSupplier(supplier: Supplier): Int

    // Settings
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSettings(settings: AppSettings): Long
}

@Database(
    entities = [
        Customer::class,
        Tx::class,
        Product::class,
        Expense::class,
        Invoice::class,
        InvoiceItem::class,
        Supplier::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountsDao(): AccountsDao
}
