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
fun DashboardScreen(viewModel: MainViewModel, navController: NavController, onOpenDrawer: () -> Unit) {
    AccountsScreen(viewModel, navController, onOpenDrawer)
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
fun AccountsScreen(viewModel: MainViewModel, navController: NavController, onOpenDrawer: () -> Unit) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCustomers = customers.filter { cust ->
        cust.name.contains(searchQuery, ignoreCase = true) || cust.phone.contains(searchQuery)
    }

    val totalDebt = customers.filter { it.balance > 0 }.sumOf { it.balance }
    val totalCredit = customers.filter { it.balance < 0 }.sumOf { Math.abs(it.balance) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("دين البقاله للعملاء", fontWeight = FontWeight.Normal, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "القائمة", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, "بحث", tint = Color.White)
                    }
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, "تنبيهات", tint = Color.White)
                    }
                    IconButton(onClick = { /* Sort */ }) {
                        Icon(Icons.Default.Sort, "ترتيب", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "لك:= ${formatMoney(totalDebt)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        VerticalDivider(
                            modifier = Modifier.height(20.dp),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "عليك:= ${formatMoney(totalCredit)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "يمني",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.Add, "إضافة", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCustomers, key = { it.id }) { customer ->
                    val txCount = transactions.count { it.customerId == customer.id }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("customer_ledger/${customer.id}") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Red/Green arrow triangle
                                Icon(
                                    if (customer.balance >= 0) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp,
                                    contentDescription = null,
                                    tint = if (customer.balance >= 0) DebtRed else CreditGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Balance
                                Text(
                                    formatMoney(Math.abs(customer.balance)),
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    modifier = Modifier.width(80.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Counter circle (blue)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF90CAF9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        txCount.toString(),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Name
                                Text(
                                    customer.name,
                                    fontWeight = FontWeight.Normal,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            // Plus button on the right
                            IconButton(onClick = { /* Quick Add */ }) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "إضافة سريعة",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
        
        if (showAddDialog) {
            CustomerDialog(
                customer = null,
                onDismiss = { showAddDialog = false },
                onSave = {
                    viewModel.saveCustomer(it)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(customerId: Int, viewModel: MainViewModel, navController: NavController, onOpenDrawer: () -> Unit) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    
    val customer = customers.find { it.id == customerId }
    val custTxs = transactions.filter { it.customerId == customerId }.sortedBy { it.id }
    
    // Calculate running balance for the table
    var currentRunning = 0.0
    val txsWithBalance = custTxs.map { tx ->
        if (tx.type == TransactionType.DEBIT) currentRunning += tx.amount
        else currentRunning -= tx.amount
        tx to currentRunning
    }.reversed()

    val context = LocalContext.current
    var showTxDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "كشف الحساب", fontWeight = FontWeight.Normal, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, "بحث", tint = Color.White)
                    }
                    IconButton(onClick = { /* PDF */ }) {
                        Icon(Icons.Default.PictureAsPdf, "PDF", tint = Color.White)
                    }
                    IconButton(onClick = { /* More */ }) {
                        Icon(Icons.Default.MoreVert, "المزيد", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            Column {
                // Footer Totals
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("عليك:= ${formatMoney(custTxs.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount })} ", fontWeight = FontWeight.Bold)
                        Text("له:= ${formatMoney(custTxs.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount })} ", fontWeight = FontWeight.Bold)
                    }
                }
                
                // Bottom Action Bar
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.LightGray)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { /* WhatsApp */ }) {
                            Icon(Icons.Default.Share, "WhatsApp", tint = Color(0xFF25D366))
                        }
                        IconButton(onClick = { /* Phone */ }) {
                            Icon(Icons.Default.Phone, "اتصال", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { /* Money */ }) {
                            Icon(Icons.Default.AccountBalanceWallet, "رصيد", tint = Color.Gray)
                        }
                        IconButton(onClick = { /* Transfer */ }) {
                            Icon(Icons.Default.SyncAlt, "تحويل", tint = Color.Gray)
                        }
                        
                        FloatingActionButton(
                            onClick = { showTxDialog = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, "إضافة حركة")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Table Header
            Surface(
                color = Color(0xFFF5F5F5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الرصيد", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("التفاصيل", modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("المبلغ", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("التاريخ", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(txsWithBalance) { (tx, balance) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Balance
                        Text(
                            formatMoney(Math.abs(balance)),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        
                        // Details
                        Text(
                            tx.note,
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Amount with direction icon
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (tx.type == TransactionType.DEBIT) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp,
                                contentDescription = null,
                                tint = if (tx.type == TransactionType.DEBIT) DebtRed else CreditGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                formatMoney(tx.amount),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type == TransactionType.DEBIT) DebtRed else CreditGreen
                            )
                        }
                        
                        // Date
                        Text(
                            tx.date.substringAfter("-"), // Show only month/day or simplified
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
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
