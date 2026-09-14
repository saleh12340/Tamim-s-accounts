package com.aistudio.tamimsaccounts

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(viewModel: MainViewModel, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المزيد والخدمات المساندة", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MoreMenuCard(
                    title = "سجل المصروفات اليومية",
                    subtitle = "تسجيل فواتير الكهرباء، الإيجار، النثريات، والرواتب",
                    icon = Icons.Default.TrendingDown,
                    badgeColor = DebtRedLight,
                    iconColor = DebtRed
                ) {
                    navController.navigate("expenses")
                }
            }

            item {
                MoreMenuCard(
                    title = "الفواتير والمبيعات",
                    subtitle = "إنشاء فواتير بيع وشراء، طباعة ومشاركة الإيصالات",
                    icon = Icons.Default.ReceiptLong,
                    badgeColor = Color(0xFFE0F2FE),
                    iconColor = Color(0xFF0284C7)
                ) {
                    navController.navigate("invoices")
                }
            }

            item {
                MoreMenuCard(
                    title = "التقارير المالية الشاملة",
                    subtitle = "إحصائيات الأرباح، مديونيات العملاء، وجرد الصندوق",
                    icon = Icons.Default.Assessment,
                    badgeColor = EmeraldLight,
                    iconColor = EmeraldPrimary
                ) {
                    navController.navigate("reports")
                }
            }

            item {
                MoreMenuCard(
                    title = "إدارة المخزون والأصناف",
                    subtitle = "أسعار السلع، مراقبة النواقص، وتعديل الكميات",
                    icon = Icons.Default.Inventory2,
                    badgeColor = Color(0xFFFEF9C3),
                    iconColor = Color(0xFFCA8A04)
                ) {
                    navController.navigate("products")
                }
            }

            item {
                MoreMenuCard(
                    title = "الإعدادات وبيانات المحل",
                    subtitle = "اسم البقالة، رقم الهاتف، النسخ الاحتياطي والبيانات",
                    icon = Icons.Default.Settings,
                    badgeColor = Color(0xFFF1F5F9),
                    iconColor = Color(0xFF475569)
                ) {
                    navController.navigate("settings")
                }
            }
        }
    }
}

@Composable
fun MoreMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: MainViewModel, navController: NavController) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val totalExpenses = expenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل المصروفات والنثريات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    expenseToEdit = null
                    showDialog = true
                },
                icon = { Icon(Icons.Default.Add, "إضافة") },
                text = { Text("تسجيل مصروف", fontWeight = FontWeight.Bold) },
                containerColor = DebtRed,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("إجمالي المصروفات المسجلة", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatMoney(totalExpenses)} ريال",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = DebtRed
                    )
                    Text("عدد العمليات: ${expenses.size}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد مصروفات مسجلة", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(DebtRedLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = DebtRed, modifier = Modifier.size(22.dp))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(expense.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    if (expense.note.isNotBlank()) {
                                        Text(expense.note, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    }
                                    Text(expense.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${formatMoney(expense.amount)} ريال",
                                        fontWeight = FontWeight.Bold,
                                        color = DebtRed,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    IconButton(
                                        onClick = { expenseToDelete = expense },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            ExpenseDialog(
                expense = expenseToEdit,
                onDismiss = {
                    showDialog = false
                    expenseToEdit = null
                },
                onSave = {
                    viewModel.saveExpense(it)
                    showDialog = false
                    expenseToEdit = null
                }
            )
        }

        if (expenseToDelete != null) {
            ConfirmDeleteDialog(
                title = "حذف المصروف",
                message = "هل أنت متأكد من حذف مصروف '${expenseToDelete?.title}' بقيمة ${expenseToDelete?.amount} ريال؟",
                onConfirm = {
                    expenseToDelete?.let { viewModel.deleteExpense(it) }
                    expenseToDelete = null
                },
                onDismiss = { expenseToDelete = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(viewModel: MainViewModel, navController: NavController) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCreateInvoiceDialog by remember { mutableStateOf(false) }
    var invoiceToDelete by remember { mutableStateOf<Invoice?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الفواتير والمبيعات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateInvoiceDialog = true },
                icon = { Icon(Icons.Default.ReceiptLong, "إصدار") },
                text = { Text("فاتورة جديدة", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (invoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد فواتير مصدرة حتى الآن", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("اضغط على 'فاتورة جديدة' لإنشاء أول فاتورة بيع", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(invoices, key = { it.id }) { invoice ->
                        val isPaidInFull = invoice.paid >= invoice.total
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (invoice.type == InvoiceType.SALE) EmeraldLight else Color(0xFFE0F2FE)
                                        ) {
                                            Text(
                                                if (invoice.type == InvoiceType.SALE) "فاتورة بيع" else "فاتورة شراء",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (invoice.type == InvoiceType.SALE) EmeraldPrimary else Color(0xFF0284C7)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("#${invoice.id}", fontWeight = FontWeight.Bold, color = Color.Gray)
                                    }

                                    Text(invoice.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            invoice.customerOrSupplierName ?: "زبون عام",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (invoice.note.isNotBlank()) {
                                            Text(invoice.note, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${formatMoney(invoice.total)} ريال",
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            if (isPaidInFull) "مدفوعة بالكامل (كاش)" else "متبقي آجل: ${formatMoney(invoice.total - invoice.paid)} ريال",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPaidInFull) CreditGreen else DebtRed
                                        )
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Share text receipt
                                    TextButton(
                                        onClick = {
                                            val storeName = settings?.storeName ?: "بقالة العزي"
                                            val sb = StringBuilder()
                                            sb.append("🧾 *فاتورة #${invoice.id} - $storeName*\n")
                                            sb.append("👤 العميل: *${invoice.customerOrSupplierName ?: "زبون عام"}*\n")
                                            sb.append("📅 التاريخ: ${invoice.date}\n")
                                            sb.append("💵 الإجمالي: *${formatMoney(invoice.total)} ريال*\n")
                                            sb.append("💳 المدفوع: ${formatMoney(invoice.paid)} ريال\n")
                                            if (!isPaidInFull) {
                                                sb.append("⚠️ المتبقي آجل: *${formatMoney(invoice.total - invoice.paid)} ريال*\n")
                                            }
                                            sb.append("\nشكراً لتسوقكم معنا 🌟")

                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, sb.toString())
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الفاتورة"))
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("مشاركة الفاتورة")
                                    }

                                    IconButton(onClick = { invoiceToDelete = invoice }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreateInvoiceDialog) {
            CreateInvoiceDialog(
                customers = customers,
                products = products,
                onDismiss = { showCreateInvoiceDialog = false },
                onSave = { invoice, items ->
                    viewModel.saveInvoiceWithItems(invoice, items, updateStockAndCustomer = true)
                    showCreateInvoiceDialog = false
                }
            )
        }

        if (invoiceToDelete != null) {
            ConfirmDeleteDialog(
                title = "حذف الفاتورة",
                message = "هل أنت متأكد من رغبتك في حذف الفاتورة رقم #${invoiceToDelete?.id}؟",
                onConfirm = {
                    invoiceToDelete?.let { viewModel.deleteInvoice(it) }
                    invoiceToDelete = null
                },
                onDismiss = { invoiceToDelete = null }
            )
        }
    }
}

@Composable
fun CreateInvoiceDialog(
    customers: List<Customer>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSave: (Invoice, List<InvoiceItem>) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerNameInput by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var paidAmountStr by remember { mutableStateOf("") }
    var isFullyPaid by remember { mutableStateOf(true) }

    // Line items list
    var invoiceItems by remember { mutableStateOf(listOf<InvoiceItem>()) }

    // Item input state
    var selectedProductForAdd by remember { mutableStateOf<Product?>(null) }
    var customItemName by remember { mutableStateOf("") }
    var itemQtyStr by remember { mutableStateOf("1") }
    var itemPriceStr by remember { mutableStateOf("") }

    val totalAmount = invoiceItems.sumOf { it.total }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إنشاء فاتورة بيع جديدة", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer selector
                Text("العميل / المشتري:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = if (selectedCustomer != null) selectedCustomer!!.name else customerNameInput,
                    onValueChange = {
                        customerNameInput = it
                        selectedCustomer = null
                    },
                    label = { Text("اسم العميل أو اتركه لزبون كاش") },
                    placeholder = { Text("زبون نقدي عام") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (customers.isNotEmpty()) {
                    Text("أو اختر عميلاً مسجلاً:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(customers) { cust ->
                            FilterChip(
                                selected = selectedCustomer?.id == cust.id,
                                onClick = {
                                    selectedCustomer = if (selectedCustomer?.id == cust.id) null else cust
                                },
                                label = { Text(cust.name) }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Add Items Section
                Text("إضافة أصناف للفاتورة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

                if (products.isNotEmpty()) {
                    Text("اختر من المخزون:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(products) { prod ->
                            SuggestionChip(
                                onClick = {
                                    selectedProductForAdd = prod
                                    customItemName = prod.name
                                    itemPriceStr = prod.sellPrice.toString()
                                },
                                label = { Text("${prod.name} (${prod.sellPrice} ر)") }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customItemName,
                    onValueChange = { customItemName = it },
                    label = { Text("اسم الصنف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = itemQtyStr,
                        onValueChange = { itemQtyStr = it },
                        label = { Text("الكمية") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = itemPriceStr,
                        onValueChange = { itemPriceStr = it },
                        label = { Text("السعر الفردي") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val qty = itemQtyStr.toIntOrNull() ?: 1
                        val price = itemPriceStr.toDoubleOrNull() ?: 0.0
                        if (customItemName.isNotBlank() && qty > 0 && price > 0) {
                            val newItem = InvoiceItem(
                                invoiceId = 0,
                                itemName = customItemName.trim(),
                                quantity = qty,
                                unitPrice = price,
                                total = qty * price
                            )
                            invoiceItems = invoiceItems + newItem
                            // Reset inputs
                            customItemName = ""
                            itemQtyStr = "1"
                            itemPriceStr = ""
                            selectedProductForAdd = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = customItemName.isNotBlank() && (itemPriceStr.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة الصنف إلى الفاتورة")
                }

                // Current items list in dialog
                if (invoiceItems.isNotEmpty()) {
                    Text("أصناف الفاتورة (${invoiceItems.size}):", fontWeight = FontWeight.Bold)
                    invoiceItems.forEachIndexed { index, item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.itemName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${item.quantity} × ${formatMoney(item.unitPrice)} ريال", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                Text("${formatMoney(item.total)} ريال", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                IconButton(
                                    onClick = { invoiceItems = invoiceItems.filterIndexed { i, _ -> i != index } },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Surface(
                        color = EmeraldLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي الفاتورة:", fontWeight = FontWeight.Bold)
                            Text("${formatMoney(totalAmount)} ريال", fontWeight = FontWeight.Black, color = EmeraldPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    // Payment details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isFullyPaid,
                            onClick = { isFullyPaid = true },
                            label = { Text("مدفوعة نقداً (كاش)") }
                        )
                        FilterChip(
                            selected = !isFullyPaid,
                            onClick = { isFullyPaid = false },
                            label = { Text("آجلة / سداد جزئي") }
                        )
                    }

                    if (!isFullyPaid) {
                        OutlinedTextField(
                            value = paidAmountStr,
                            onValueChange = { paidAmountStr = it },
                            label = { Text("المبلغ المدفوع حالياً (ريال)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (invoiceItems.isNotEmpty()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                        val date = sdf.format(Date())
                        val clientName = selectedCustomer?.name ?: customerNameInput.ifBlank { "زبون نقدي عام" }
                        val paidVal = if (isFullyPaid) totalAmount else (paidAmountStr.toDoubleOrNull() ?: 0.0)

                        val invoice = Invoice(
                            type = InvoiceType.SALE,
                            customerId = selectedCustomer?.id,
                            customerOrSupplierName = clientName,
                            date = date,
                            total = totalAmount,
                            paid = paidVal,
                            note = note
                        )
                        onSave(invoice, invoiceItems)
                    }
                },
                enabled = invoiceItems.isNotEmpty()
            ) {
                Text("حفظ وإصدار الفاتورة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: MainViewModel, navController: NavController) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()

    val totalDebt = customers.filter { it.balance > 0 }.sumOf { it.balance }
    val totalCredit = customers.filter { it.balance < 0 }.sumOf { Math.abs(it.balance) }
    val totalStockValuation = products.sumOf { it.sellPrice * it.stock }
    val totalStockCost = products.sumOf { it.buyPrice * it.stock }
    val totalExpenses = expenses.sumOf { it.amount }
    val totalSales = invoices.filter { it.type == InvoiceType.SALE }.sumOf { it.total }

    val topDebtors = customers.filter { it.balance > 0 }.sortedByDescending { it.balance }.take(5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التقارير المالية والإحصائيات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("ملخص المركز المالي للبقالة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        FinancialMetricRow("إجمالي ديون العملاء (لنا بالخارج):", "${formatMoney(totalDebt)} ريال", DebtRed)
                        FinancialMetricRow("أرصدة ودفعات مقدمة للعملاء:", "${formatMoney(totalCredit)} ريال", CreditGreen)
                        FinancialMetricRow("إجمالي قيمة بضاعة المخزون:", "${formatMoney(totalStockValuation)} ريال", MaterialTheme.colorScheme.primary)
                        FinancialMetricRow("إجمالي مبيعات الفواتير:", "${formatMoney(totalSales)} ريال", EmeraldMedium)
                        FinancialMetricRow("إجمالي المصروفات والنثريات:", "${formatMoney(totalExpenses)} ريال", DebtRed)
                    }
                }
            }

            // Top Debtors Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("أكثر العملاء مديونية (أولوية التحصيل)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (topDebtors.isEmpty()) {
                            Text("لا توجد ديون مسجلة على العملاء حالياً", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        } else {
                            topDebtors.forEachIndexed { index, cust ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navController.navigate("customer_ledger/${cust.id}") }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${index + 1}.", fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(cust.name, fontWeight = FontWeight.Bold)
                                            if (cust.phone.isNotBlank()) {
                                                Text(cust.phone, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            }
                                        }
                                    }

                                    Text(
                                        "${formatMoney(cust.balance)} ريال",
                                        fontWeight = FontWeight.Bold,
                                        color = DebtRed
                                    )
                                }
                                if (index < topDebtors.size - 1) {
                                    HorizontalDivider(color = Color(0xFFF8FAFC))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialMetricRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = valueColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, navController: NavController) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var storeName by remember { mutableStateOf(settings?.storeName ?: "بقالة العزي") }
    var phone by remember { mutableStateOf(settings?.phone ?: "777000000") }
    var currency by remember { mutableStateOf(settings?.currency ?: "ريال") }

    var showClearConfirm by remember { mutableStateOf(false) }
    var showPasteRestoreDialog by remember { mutableStateOf(false) }
    var restoreResultDialogMessage by remember { mutableStateOf<String?>(null) }

    // File Picker for JSON backup file
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() }
                if (!jsonString.isNullOrBlank()) {
                    viewModel.restoreBackup(jsonString) { result ->
                        result.onSuccess { summary ->
                            restoreResultDialogMessage = summary
                        }.onFailure { err ->
                            restoreResultDialogMessage = "حدث خطأ أثناء قراءة ملف النسخة الاحتياطية: ${err.localizedMessage}"
                        }
                    }
                } else {
                    Toast.makeText(context, "الملف فارغ أو غير صالح", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل فتح الملف: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(settings) {
        settings?.let {
            storeName = it.storeName
            phone = it.phone
            currency = it.currency
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات المحل وقاعدة البيانات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Store details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("بيانات المحل والبقالة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("اسم البقالة / المتجر") },
                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم هاتف المحل") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text("العملة الافتراضية") },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                viewModel.saveSettings(
                                    AppSettings(
                                        id = 1,
                                        storeName = storeName.trim(),
                                        phone = phone.trim(),
                                        currency = currency.trim()
                                    )
                                )
                                Toast.makeText(context, "تم حفظ بيانات المتجر بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ بيانات المتجر")
                        }
                    }
                }
            }

            // Backup & Restore Section (النسخ الاحتياطي واستعادة قاعدة البيانات)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("النسخ الاحتياطي واستعادة البيانات", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Text(
                            "يمكنك استعادة قاعدة البيانات المحفوظة من الموقع أو من ملف JSON، أو تصدير نسخة احتياطية جديدة لحفظها على هاتفك أو مشاركتها عبر الواتساب.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Restore from File Button
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استعادة قاعدة البيانات من ملف (JSON / ملف النسخ)")
                        }

                        // Restore by Pasting JSON Button
                        OutlinedButton(
                            onClick = { showPasteRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استعادة عبر لصق نص النسخة الاحتياطية")
                        }

                        // Export & Share Backup Button
                        FilledTonalButton(
                            onClick = {
                                viewModel.exportBackup { jsonStr ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, jsonStr)
                                        putExtra(Intent.EXTRA_TITLE, "نسخة احتياطية - بقالة العزي")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "مشاركة النسخة الاحتياطية لقاعدة البيانات")
                                    context.startActivity(shareIntent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تصدير ومشاركة نسخة احتياطية (JSON)")
                        }
                    }
                }
            }

            // Quick Data Tools & Demo Data
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("أدوات التجربة وإعادة التعيين", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        // Sample Data Button
                        FilledTonalButton(
                            onClick = {
                                viewModel.loadSampleData()
                                Toast.makeText(context, "تم تحميل البيانات التجريبية بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DownloadForOffline, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحميل بيانات تجريبية جاهزة للتجربة")
                        }

                        // Clear Data Button
                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تصفير ومسح جميع البيانات")
                        }
                    }
                }
            }

            // App About
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("بقالة العزي - دفتر الحسابات الذكي", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("الإصدار 1.0 • متوافق مع نسخ واستعادة بيانات الموقع (Offline / Room Database)", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // Paste Restore Dialog
        if (showPasteRestoreDialog) {
            PasteBackupRestoreDialog(
                onDismiss = { showPasteRestoreDialog = false },
                onRestore = { text ->
                    viewModel.restoreBackup(text) { result ->
                        showPasteRestoreDialog = false
                        result.onSuccess { summary ->
                            restoreResultDialogMessage = summary
                        }.onFailure { err ->
                            restoreResultDialogMessage = "فشلت الاستعادة: ${err.localizedMessage}"
                        }
                    }
                }
            )
        }

        // Result Dialog
        if (restoreResultDialogMessage != null) {
            AlertDialog(
                onDismissRequest = { restoreResultDialogMessage = null },
                icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("نتيجة استعادة البيانات", fontWeight = FontWeight.Bold) },
                text = { Text(restoreResultDialogMessage ?: "") },
                confirmButton = {
                    Button(onClick = { restoreResultDialogMessage = null }) {
                        Text("حسناً")
                    }
                }
            )
        }

        if (showClearConfirm) {
            ConfirmDeleteDialog(
                title = "تصفير قاعدة البيانات",
                message = "تحذير: سيتم حذف جميع العملاء، الحركات، المصروفات، والفواتير نهائياً. هل أنت متأكد؟",
                onConfirm = {
                    viewModel.clearAllData()
                    showClearConfirm = false
                    Toast.makeText(context, "تم مسح البيانات بنجاح", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showClearConfirm = false }
            )
        }
    }
}

@Composable
fun PasteBackupRestoreDialog(
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    var jsonInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("لصق نص النسخة الاحتياطية", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "قم بلصق نص الـ JSON الخاص بالنسخة الاحتياطية التي تم تصديرها من الموقع أو التطبيق:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    placeholder = { Text("{\"customers\": [...], ...}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jsonInput.isNotBlank()) {
                        onRestore(jsonInput.trim())
                    }
                },
                enabled = jsonInput.isNotBlank()
            ) {
                Text("استعادة البيانات الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
