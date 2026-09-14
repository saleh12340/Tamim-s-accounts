package com.aistudio.tamimsaccounts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.util.Locale

fun formatMoney(amount: Double): String = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2 }.format(amount)

@Composable
fun DashboardScreen(viewModel: MainViewModel, navController: NavController, onOpenDrawer: () -> Unit) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val debt = customers.filter { it.balance > 0 }.sumOf { it.balance }
    val credit = customers.filter { it.balance < 0 }.sumOf { -it.balance }
    val lowStock = products.count { it.stock <= it.minStock }
    val sales = invoices.filter { it.type == InvoiceType.SALE }.sumOf { it.total }
    Scaffold(topBar = {
        TopAppBar(title = { Text("لوحة الحسابات", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "القائمة") } }, actions = { IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Default.Settings, "الإعدادات") } })
    }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 14.dp)) {
            item { Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("ملخص الحسابات", fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { SummaryValue("علينا", debt, DebtRed); SummaryValue("لنا", credit, CreditGreen); SummaryValue("المبيعات", sales, MaterialTheme.colorScheme.primary) } } } }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { HomeAction("العملاء", Icons.Default.People, Modifier.weight(1f)) { navController.navigate("accounts") }; HomeAction("المخزون", Icons.Default.Inventory2, Modifier.weight(1f)) { navController.navigate("products") }; HomeAction("الفواتير", Icons.Default.ReceiptLong, Modifier.weight(1f)) { navController.navigate("invoices") } } }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { HomeAction("المصروفات", Icons.Default.Payments, Modifier.weight(1f)) { navController.navigate("expenses") }; HomeAction("التقارير", Icons.Default.Assessment, Modifier.weight(1f)) { navController.navigate("reports") }; HomeAction("الإعدادات", Icons.Default.Settings, Modifier.weight(1f)) { navController.navigate("settings") } } }
            item { Card(shape = RoundedCornerShape(18.dp)) { Column(Modifier.fillMaxWidth().padding(16.dp)) { Text("تنبيهات", fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(if (lowStock == 0) "✓ المخزون ضمن الحدود" else "⚠ يوجد $lowStock صنف يحتاج إلى تعبئة"); Spacer(Modifier.height(4.dp)); Text("المصروفات: ${formatMoney(expenses.sumOf { it.amount })} ريال", color = Color.Gray, fontSize = 13.sp) } } }
        }
    }
}

@Composable private fun SummaryValue(label: String, value: Double, color: Color) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(formatMoney(value), fontWeight = FontWeight.Bold, color = color, fontSize = 17.sp); Text(label, fontSize = 12.sp, color = Color.Gray) } }
@Composable private fun HomeAction(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) { Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)); Spacer(Modifier.height(7.dp)); Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: MainViewModel, navController: NavController, onOpenDrawer: () -> Unit) {
    val customers by viewModel.customers.collectAsStateWithLifecycle(); val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }; var sortByBalance by remember { mutableStateOf(false) }; var showAdd by remember { mutableStateOf(false) }; var quickCustomer by remember { mutableStateOf<Customer?>(null) }; var editCustomer by remember { mutableStateOf<Customer?>(null) }; var deleteCustomer by remember { mutableStateOf<Customer?>(null) }
    val list = customers.filter { it.name.contains(search, true) || it.phone.contains(search) }.let { if (sortByBalance) it.sortedByDescending { c -> kotlin.math.abs(c.balance) } else it.sortedBy { c -> c.name } }
    val debt = customers.filter { it.balance > 0 }.sumOf { it.balance }; val credit = customers.filter { it.balance < 0 }.sumOf { -it.balance }
    Scaffold(topBar = { TopAppBar(title = { Text("حسابات العملاء") }, navigationIcon = { IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, "القائمة") } }, actions = { IconButton(onClick = { sortByBalance = !sortByBalance }) { Icon(Icons.Default.Sort, "ترتيب") } }) }, floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.PersonAdd, "إضافة عميل") } }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), singleLine = true, label = { Text("بحث بالاسم أو الهاتف") }, leadingIcon = { Icon(Icons.Default.Search, null) }, trailingIcon = { if (search.isNotEmpty()) IconButton(onClick = { search = "" }) { Icon(Icons.Default.Clear, null) } })
            Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly) { Text("عليه ${formatMoney(debt)}", color = DebtRed, fontWeight = FontWeight.Bold); Text("له ${formatMoney(credit)}", color = CreditGreen, fontWeight = FontWeight.Bold) }
            LazyColumn(Modifier.fillMaxSize()) { items(list, key = { it.id }) { customer -> CustomerRow(customer, transactions.count { it.customerId == customer.id }, { navController.navigate("customer_ledger/${customer.id}") }, { quickCustomer = customer }, { editCustomer = customer }, { deleteCustomer = customer }) }; if (list.isEmpty()) item { EmptyState(if (search.isBlank()) "لا توجد حسابات بعد" else "لا توجد نتائج") } }
        }
    }
    if (showAdd) CustomerDialog(null, { showAdd = false }) { viewModel.saveCustomer(it); showAdd = false }
    editCustomer?.let { c -> CustomerDialog(c, { editCustomer = null }) { viewModel.saveCustomer(it); editCustomer = null } }
    quickCustomer?.let { c -> TransactionDialog(c, { quickCustomer = null }) { viewModel.saveTransaction(it, c); quickCustomer = null } }
    deleteCustomer?.let { c -> ConfirmDeleteDialog("حذف الحساب؟", "سيتم حذف الحساب وحركاته.", { viewModel.deleteCustomer(c); deleteCustomer = null }, { deleteCustomer = null }) }
}

@Composable private fun CustomerRow(customer: Customer, count: Int, onOpen: () -> Unit, onQuick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) { var menu by remember { mutableStateOf(false) }; Row(Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(CircleShape).background(if (customer.balance > 0) DebtRedLight else CreditGreenLight), Alignment.Center) { Text(customer.name.take(1), fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(customer.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("$count حركة${if (customer.phone.isNotBlank()) " • ${customer.phone}" else ""}", fontSize = 12.sp, color = Color.Gray) }; Column(horizontalAlignment = Alignment.End) { Text(formatMoney(kotlin.math.abs(customer.balance)), fontWeight = FontWeight.Bold, color = if (customer.balance > 0) DebtRed else CreditGreen); Text(if (customer.balance > 0) "عليه" else if (customer.balance < 0) "له" else "متوازن", fontSize = 11.sp, color = Color.Gray) }; IconButton(onClick = onQuick) { Icon(Icons.Default.AddCircleOutline, "إضافة حركة") }; Box { IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "المزيد") }; DropdownMenu(menu, { menu = false }) { DropdownMenuItem(text = { Text("فتح الحساب") }, onClick = { menu = false; onOpen() }); DropdownMenuItem(text = { Text("تعديل") }, onClick = { menu = false; onEdit() }); DropdownMenuItem(text = { Text("حذف") }, onClick = { menu = false; onDelete() }) } } }; HorizontalDivider() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(customerId: Int, viewModel: MainViewModel, navController: NavController, onOpenDrawer: () -> Unit) {
    val customers by viewModel.customers.collectAsStateWithLifecycle(); val transactions by viewModel.transactions.collectAsStateWithLifecycle(); val customer = customers.find { it.id == customerId }; val context = LocalContext.current; var add by remember { mutableStateOf(false) }; var deleteTx by remember { mutableStateOf<Tx?>(null) }
    if (customer == null) { LaunchedEffect(Unit) { navController.navigateUp() }; return }
    val txs = transactions.filter { it.customerId == customerId }.sortedBy { it.id }; var running = 0.0; val rows = txs.map { tx -> running += if (tx.type == TransactionType.DEBIT) tx.amount else -tx.amount; tx to running }.reversed()
    Scaffold(topBar = { TopAppBar(title = { Text(customer.name, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "عودة") } }, actions = { IconButton(onClick = { shareStatement(context, customer, txs) }) { Icon(Icons.Default.Share, "مشاركة") }; if (customer.phone.isNotBlank()) IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))) }) { Icon(Icons.Default.Phone, "اتصال") } }) }, floatingActionButton = { FloatingActionButton(onClick = { add = true }) { Icon(Icons.Default.Add, "إضافة حركة") } }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            Card(Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(18.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("الرصيد", color = Color.Gray, fontSize = 12.sp); Text(formatMoney(kotlin.math.abs(customer.balance)), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (customer.balance > 0) DebtRed else CreditGreen) }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("الحركات", color = Color.Gray, fontSize = 12.sp); Text(txs.size.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold) } } }
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp)) { Text("الرصيد", Modifier.weight(1f), TextAlign.Center, fontWeight = FontWeight.Bold); Text("البيان", Modifier.weight(1.6f), TextAlign.Center, fontWeight = FontWeight.Bold); Text("المبلغ", Modifier.weight(1f), TextAlign.Center, fontWeight = FontWeight.Bold); Text("التاريخ", Modifier.weight(1.2f), TextAlign.Center, fontWeight = FontWeight.Bold) }
            LazyColumn(Modifier.fillMaxSize()) { items(rows, key = { it.first.id }) { (tx, balance) -> Row(Modifier.fillMaxWidth().clickable { deleteTx = tx }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Text(formatMoney(kotlin.math.abs(balance)), Modifier.weight(1f), TextAlign.Center, fontSize = 12.sp); Text(tx.note.ifBlank { "بدون بيان" }, Modifier.weight(1.6f), TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp); Text((if (tx.type == TransactionType.DEBIT) "− " else "+ ") + formatMoney(tx.amount), Modifier.weight(1f), TextAlign.Center, fontWeight = FontWeight.Bold, color = if (tx.type == TransactionType.DEBIT) DebtRed else CreditGreen, fontSize = 12.sp); Text(tx.date.takeLast(11), Modifier.weight(1.2f), TextAlign.Center, color = Color.Gray, fontSize = 11.sp) }; HorizontalDivider() }; if (rows.isEmpty()) item { EmptyState("لا توجد حركات لهذا الحساب") } }
        }
    }
    if (add) TransactionDialog(customer, { add = false }) { viewModel.saveTransaction(it, customer); add = false }
    deleteTx?.let { tx -> ConfirmDeleteDialog("حذف الحركة؟", "سيتم عكس أثر الحركة على رصيد العميل.", { viewModel.deleteTransaction(tx, customer); deleteTx = null }, { deleteTx = null }) }
}

private fun shareStatement(context: android.content.Context, customer: Customer, txs: List<Tx>) { val body = buildString { append("كشف حساب: ${customer.name}\n"); if (customer.phone.isNotBlank()) append("الهاتف: ${customer.phone}\n"); append("الرصيد الحالي: ${formatMoney(customer.balance)} ريال\n\n"); txs.forEach { append("${it.date} — ${it.note} — ${formatMoney(it.amount)} ريال — ${if (it.type == TransactionType.DEBIT) "عليه" else "سداد"}\n") } }; context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, body) }, "مشاركة كشف الحساب")) }
@Composable private fun EmptyState(text: String) { Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { Text(text, color = Color.Gray) } }
