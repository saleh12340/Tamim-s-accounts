package com.aistudio.tamimsaccounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, navController: NavController) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    
    val totalDebt = customers.filter { it.balance > 0 }.sumOf { it.balance }
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("الرئيسية", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إجمالي الديون (لنا)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "$totalDebt ريال", 
                        style = MaterialTheme.typography.headlineLarge, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Text(
                "أحدث العمليات", 
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyColumn {
                items(transactions.take(15)) { tx ->
                    val customer = customers.find { it.id == tx.customerId }
                    ListItem(
                        headlineContent = { Text(customer?.name ?: "عميل محذوف", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${tx.note} • ${tx.date}") },
                        trailingContent = { 
                            Text(
                                "${tx.amount}", 
                                color = if(tx.type == TransactionType.DEBIT) Color(0xFFD32F2F) else Color(0xFF388E3C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: MainViewModel, navController: NavController) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("العملاء", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingCustomer = null
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة عميل")
            }
        }
    ) { padding ->
        if (customers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("لا يوجد عملاء. أضف عميلاً جديداً.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(customers) { customer ->
                    ListItem(
                        modifier = Modifier.clickable { 
                            navController.navigate("customer_ledger/${customer.id}") 
                        },
                        headlineContent = { Text(customer.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(customer.phone.ifBlank { "لا يوجد رقم هاتف" }) },
                        trailingContent = { 
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${customer.balance}",
                                    color = if (customer.balance > 0) Color(0xFFD32F2F) else if (customer.balance < 0) Color(0xFF388E3C) else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(if (customer.balance > 0) "عليه" else if (customer.balance < 0) "له" else "خالص", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
        
        if (showDialog) {
            CustomerDialog(
                customer = editingCustomer,
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
fun ProductsScreen(viewModel: MainViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("المخزون", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO Add Product */ }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة منتج")
            }
        }
    ) { padding ->
        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("المخزون فارغ. أضف منتجات جديدة.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(products) { p ->
                    ListItem(
                        headlineContent = { Text(p.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("الكمية: ${p.stock}") },
                        trailingContent = { Text("${p.sellPrice} ريال", fontWeight = FontWeight.Bold) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(viewModel: MainViewModel, navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("المزيد", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(headlineContent = { Text("المصروفات") }, modifier = Modifier.clickable {})
            HorizontalDivider()
            ListItem(headlineContent = { Text("الفواتير") }, modifier = Modifier.clickable {})
            HorizontalDivider()
            ListItem(headlineContent = { Text("الإعدادات") }, modifier = Modifier.clickable {})
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(customerId: Int, viewModel: MainViewModel, navController: NavController) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    
    val customer = customers.find { it.id == customerId }
    val custTxs = transactions.filter { it.customerId == customerId }
    
    var showTxDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "تفاصيل الحساب", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة")
                    }
                }
            )
        },
        floatingActionButton = {
            if (customer != null) {
                FloatingActionButton(onClick = { showTxDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "عملية جديدة")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (customer != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (customer.balance > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الرصيد الحالي", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${Math.abs(customer.balance)} ريال", 
                            style = MaterialTheme.typography.headlineMedium, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(if (customer.balance > 0) "عليه (مدين)" else if (customer.balance < 0) "له (دائن)" else "خالص")
                    }
                }
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(custTxs) { tx ->
                        ListItem(
                            headlineContent = { 
                                Text(
                                    "${tx.amount} ريال", 
                                    color = if(tx.type == TransactionType.DEBIT) Color(0xFFD32F2F) else Color(0xFF388E3C),
                                    fontWeight = FontWeight.Bold
                                ) 
                            },
                            supportingContent = { Text("${tx.note}\n${tx.date}") },
                            trailingContent = {
                                IconButton(onClick = { 
                                    viewModel.deleteTransaction(tx, customer)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف العملية", tint = Color.Gray)
                                }
                            }
                        )
                        HorizontalDivider()
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
    }
}
