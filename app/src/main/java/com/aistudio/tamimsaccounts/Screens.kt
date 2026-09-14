package com.aistudio.tamimsaccounts

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 2
    return formatter.format(amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, navController: NavController) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val totalDebt = customers.filter { it.balance > 0 }.sumOf { it.balance }
    val totalCredit = customers.filter { it.balance < 0 }.sumOf { Math.abs(it.balance) }
    val lowStockCount = products.count { it.stock <= it.minStock }

    var showNewCustomerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            settings?.storeName ?: "بقالة العزي",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        val today = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar")).format(Date())
                        Text(today, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("reports") }) {
                        Icon(Icons.Outlined.Assessment, contentDescription = "التقارير", tint = Color.White)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "الإعدادات", tint = Color.White)
                    }
                },
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Hero Financial Banner
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = DebtRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "إجمالي ديون العملاء (لنا في السوق)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "${formatMoney(totalDebt)} ريال",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontSize = 28.sp),
                                    color = DebtRed
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("أرصدة للعملاء (علينا)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            "${formatMoney(totalCredit)} ريال",
                                            fontWeight = FontWeight.Bold,
                                            color = CreditGreen,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("إجمالي العملاء", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            "${customers.size} عميل",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Low stock alert banner (if any)
            if (lowStockCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { navController.navigate("products") },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تنبيه المخزون المنخفض", fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                Text("يوجد $lowStockCount أصناف وصلت لحد الطلب أو نفدت", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB45309))
                            }
                            Text("عرض", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }
                    }
                }
            }

            // Quick Actions Bar
            item {
                Text(
                    "إجراءات سريعة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "عميل جديد",
                        icon = Icons.Default.PersonAdd,
                        bgColor = EmeraldLight,
                        iconColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    ) {
                        showNewCustomerDialog = true
                    }

                    QuickActionButton(
                        title = "فاتورة بيع",
                        icon = Icons.Default.ReceiptLong,
                        bgColor = Color(0xFFE0F2FE),
                        iconColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate("invoices")
                    }

                    QuickActionButton(
                        title = "تسجيل مصروف",
                        icon = Icons.Default.TrendingDown,
                        bgColor = Color(0xFFFEE2E2),
                        iconColor = DebtRed,
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate("expenses")
                    }

                    QuickActionButton(
                        title = "المخزون",
                        icon = Icons.Default.Inventory2,
                        bgColor = Color(0xFFFEF9C3),
                        iconColor = Color(0xFFCA8A04),
                        modifier = Modifier.weight(1f)
                    ) {
                        navController.navigate("products")
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "آخر حركات الدفتر",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { navController.navigate("accounts") }) {
                        Text("عرض كل العملاء")
                    }
                }
            }

            // Transactions list
            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("لا توجد حركات مسجلة حتى الآن", fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("ابدأ بتسجيل ديون أو سدادات للعملاء", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(transactions.take(12)) { tx ->
                    val customer = customers.find { it.id == tx.customerId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable {
                                customer?.let { navController.navigate("customer_ledger/${it.id}") }
                            },
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (tx.type == TransactionType.DEBIT) DebtRedLight else CreditGreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (tx.type == TransactionType.DEBIT) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = if (tx.type == TransactionType.DEBIT) DebtRed else CreditGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            headlineContent = {
                                Text(
                                    customer?.name ?: "عميل غير معروف",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                Text(
                                    "${tx.note} • ${tx.date}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingContent = {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${if (tx.type == TransactionType.DEBIT) "+" else "-"}${formatMoney(tx.amount)}",
                                        color = if (tx.type == TransactionType.DEBIT) DebtRed else CreditGreen,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        if (tx.type == TransactionType.DEBIT) "عليه (دين)" else "له (سداد)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (tx.type == TransactionType.DEBIT) DebtRed else CreditGreen
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showNewCustomerDialog) {
            CustomerDialog(
                customer = null,
                onDismiss = { showNewCustomerDialog = false },
                onSave = {
                    viewModel.saveCustomer(it)
                    showNewCustomerDialog = false
                }
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: MainViewModel, navController: NavController) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // all, debit, credit, zero
    var showDialog by remember { mutableStateOf(false) }

    val filteredCustomers = customers.filter { cust ->
        val matchesQuery = cust.name.contains(searchQuery, ignoreCase = true) || cust.phone.contains(searchQuery)
        val matchesFilter = when (selectedFilter) {
            "debit" -> cust.balance > 0
            "credit" -> cust.balance < 0
            "zero" -> cust.balance == 0.0
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("دفتر الحسابات والعملاء", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, "إضافة") },
                text = { Text("عميل جديد", fontWeight = FontWeight.Bold) },
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
            // Search field
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث باسم العميل أو رقم الهاتف...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "all",
                            onClick = { selectedFilter = "all" },
                            label = { Text("الكل (${customers.size})") }
                        )
                        FilterChip(
                            selected = selectedFilter == "debit",
                            onClick = { selectedFilter = "debit" },
                            label = { Text("عليهم ديون (${customers.count { it.balance > 0 }})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DebtRedLight,
                                selectedLabelColor = DebtRed
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "credit",
                            onClick = { selectedFilter = "credit" },
                            label = { Text("لهم رصيد (${customers.count { it.balance < 0 }})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CreditGreenLight,
                                selectedLabelColor = CreditGreen
                            )
                        )
                    }
                }
            }

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.People, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا يوجد عملاء مطابقين للبحث", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("customer_ledger/${customer.id}") },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        customer.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        customer.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    if (customer.phone.isNotBlank()) {
                                        Text(
                                            customer.phone,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    if (customer.notes.isNotBlank()) {
                                        Text(
                                            customer.notes,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.DarkGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val isDebt = customer.balance > 0
                                    val isCredit = customer.balance < 0

                                    Text(
                                        "${formatMoney(Math.abs(customer.balance))} ريال",
                                        color = if (isDebt) DebtRed else if (isCredit) CreditGreen else Color.Gray,
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDebt) DebtRedLight else if (isCredit) CreditGreenLight else Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            if (isDebt) "عليه (دين)" else if (isCredit) "له (رصيد)" else "خالص",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDebt) DebtRed else if (isCredit) CreditGreen else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            CustomerDialog(
                customer = null,
                onDismiss = { showDialog = false },
                onSave = {
                    viewModel.saveCustomer(it)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(customerId: Int, viewModel: MainViewModel, navController: NavController) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val customer = customers.find { it.id == customerId }
    val custTxs = transactions.filter { it.customerId == customerId }

    var showTxDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("all") } // all, debit, credit

    val filteredTxs = custTxs.filter {
        when (filterType) {
            "debit" -> it.type == TransactionType.DEBIT
            "credit" -> it.type == TransactionType.CREDIT
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "كشف الحساب", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة")
                    }
                },
                actions = {
                    if (customer != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل بيانات العميل")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف العميل", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (customer != null) {
                ExtendedFloatingActionButton(
                    onClick = { showTxDialog = true },
                    icon = { Icon(Icons.Default.AddCircle, "إضافة") },
                    text = { Text("تسجيل حركة جديدة", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (customer != null) {
                // Customer Summary & Quick Actions Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (customer.balance > 0) DebtRedLight 
                                         else if (customer.balance < 0) CreditGreenLight 
                                         else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (customer.balance > 0) "إجمالي المبلغ المستحق (عليه)" 
                            else if (customer.balance < 0) "رصيد دائن متبقي (له)" 
                            else "الحساب خالص ومسدد",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (customer.balance > 0) DebtRed else if (customer.balance < 0) CreditGreen else Color.Gray
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            "${formatMoney(Math.abs(customer.balance))} ريال",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, fontSize = 32.sp),
                            color = if (customer.balance > 0) DebtRed else if (customer.balance < 0) CreditGreen else Color.DarkGray
                        )

                        if (customer.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("ملاحظة: ${customer.notes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Action Buttons: Call, WhatsApp, Share Statement
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Phone Call
                            FilledTonalButton(
                                onClick = {
                                    if (customer.phone.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                        context.startActivity(intent)
                                    }
                                },
                                enabled = customer.phone.isNotBlank()
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اتصال")
                            }

                            // Share WhatsApp Statement
                            Button(
                                onClick = {
                                    val storeName = settings?.storeName ?: "بقالة العزي"
                                    val statusStr = if (customer.balance > 0) "المبلغ المتبقي عليكم هو: ${formatMoney(customer.balance)} ريال" 
                                                   else if (customer.balance < 0) "لكم رصيد متبقي لدينا: ${formatMoney(Math.abs(customer.balance))} ريال" 
                                                   else "حسابكم مسدد وخالص تماماً"
                                    
                                    val sb = StringBuilder()
                                    sb.append("📋 *كشف حساب من: $storeName*\n")
                                    sb.append("👤 العميل: *${customer.name}*\n")
                                    sb.append("💵 $statusStr\n\n")
                                    sb.append("--- تفاصيل آخر العمليات ---\n")
                                    custTxs.take(5).forEach { tx ->
                                        val type = if (tx.type == TransactionType.DEBIT) "عليه (دين)" else "له (سداد)"
                                        sb.append("• ${tx.date} | ${formatMoney(tx.amount)} ريال ($type) - ${tx.note}\n")
                                    }
                                    sb.append("\nشاكرين لكم حسن التعامل 🙏")

                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, sb.toString())
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة كشف الحساب عبر"))
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة الكشف")
                            }
                        }
                    }
                }

                // Filter & Header for Operations List
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "سجل الحركات (${filteredTxs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = filterType == "all",
                            onClick = { filterType = "all" },
                            label = { Text("الكل") }
                        )
                        FilterChip(
                            selected = filterType == "debit",
                            onClick = { filterType = "debit" },
                            label = { Text("ديون") }
                        )
                        FilterChip(
                            selected = filterType == "credit",
                            onClick = { filterType = "credit" },
                            label = { Text("سدادات") }
                        )
                    }
                }

                if (filteredTxs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد حركات مسجلة لهذا العميل", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTxs, key = { it.id }) { tx ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (tx.type == TransactionType.DEBIT) DebtRedLight else CreditGreenLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (tx.type == TransactionType.DEBIT) Icons.Default.Add else Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (tx.type == TransactionType.DEBIT) DebtRed else CreditGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    headlineContent = {
                                        Text(
                                            "${if (tx.type == TransactionType.DEBIT) "+" else "-"}${formatMoney(tx.amount)} ريال",
                                            color = if (tx.type == TransactionType.DEBIT) DebtRed else CreditGreen,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    },
                                    supportingContent = {
                                        Text("${tx.note}\n${tx.date}", style = MaterialTheme.typography.bodySmall)
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.deleteTransaction(tx, customer) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف العملية", tint = Color.Gray)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("العميل غير موجود")
                }
            }
        }

        if (showTxDialog && customer != null) {
            TransactionDialog(
                customer = customer,
                onDismiss = { showTxDialog = false },
                onSave = {
                    viewModel.saveTransaction(it, customer)
                    showTxDialog = false
                }
            )
        }

        if (showEditDialog && customer != null) {
            CustomerDialog(
                customer = customer,
                onDismiss = { showEditDialog = false },
                onSave = {
                    viewModel.saveCustomer(it)
                    showEditDialog = false
                }
            )
        }

        if (showDeleteConfirm && customer != null) {
            ConfirmDeleteDialog(
                title = "حذف العميل",
                message = "هل أنت متأكد من رغبتك في حذف العميل '${customer.name}' وسجله بالكامل؟",
                onConfirm = {
                    viewModel.deleteCustomer(customer)
                    showDeleteConfirm = false
                    navController.navigateUp()
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}
