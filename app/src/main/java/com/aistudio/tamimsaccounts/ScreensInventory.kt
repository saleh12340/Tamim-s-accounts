package com.aistudio.tamimsaccounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(viewModel: MainViewModel, navController: NavController) {
    val products by viewModel.products.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("all") } // all, low, in_stock
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val totalBuyValue = products.sumOf { it.buyPrice * it.stock }
    val totalSellValue = products.sumOf { it.sellPrice * it.stock }
    val lowStockCount = products.count { it.stock <= it.minStock }

    val filteredProducts = products.filter { prod ->
        val matchesQuery = prod.name.contains(searchQuery, ignoreCase = true) || prod.barcode.contains(searchQuery)
        val matchesFilter = when (filterStatus) {
            "low" -> prod.stock <= prod.minStock
            "in_stock" -> prod.stock > prod.minStock
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة المخزون والأصناف", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    productToEdit = null
                    showDialog = true
                },
                icon = { Icon(Icons.Default.AddShoppingCart, "إضافة") },
                text = { Text("صنف جديد", fontWeight = FontWeight.Bold) },
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
            // Inventory Value Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "إجمالي قيمة بضاعة المحل (المخزون)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatMoney(totalSellValue)} ريال",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("تكلفة الشراء", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${formatMoney(totalBuyValue)} ريال", fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("الربح المتوقع", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${formatMoney((totalSellValue - totalBuyValue).coerceAtLeast(0.0))} ريال", fontWeight = FontWeight.Bold, color = CreditGreen)
                        }
                        Column {
                            Text("أصناف منخفضة", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("$lowStockCount صنف", fontWeight = FontWeight.Bold, color = if (lowStockCount > 0) DebtRed else Color.DarkGray)
                        }
                    }
                }
            }

            // Search and Filter Header
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث باسم المنتج أو الباركود...") },
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterStatus == "all",
                            onClick = { filterStatus = "all" },
                            label = { Text("الكل (${products.size})") }
                        )
                        FilterChip(
                            selected = filterStatus == "low",
                            onClick = { filterStatus = "low" },
                            label = { Text("منخفض أو نفد ($lowStockCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = Color(0xFFB45309)
                            )
                        )
                        FilterChip(
                            selected = filterStatus == "in_stock",
                            onClick = { filterStatus = "in_stock" },
                            label = { Text("متوفر (${products.size - lowStockCount})") }
                        )
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد أصناف مسجلة", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("اضغط على 'صنف جديد' لإضافة منتجاتك", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val isLowStock = product.stock <= product.minStock
                        val isOutOfStock = product.stock == 0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isOutOfStock) Color(0xFFFEE2E2)
                                                else if (isLowStock) Color(0xFFFEF3C7)
                                                else Color(0xFFE8F5E9)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ShoppingBag,
                                            contentDescription = null,
                                            tint = if (isOutOfStock) DebtRed else if (isLowStock) Color(0xFFD97706) else CreditGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            product.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (product.barcode.isNotBlank()) {
                                            Text(
                                                "باركود: ${product.barcode}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    // Edit and Delete icons
                                    IconButton(
                                        onClick = {
                                            productToEdit = product
                                            showDialog = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    }

                                    IconButton(
                                        onClick = { productToDelete = product },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF8FAFC))

                                // Prices and Stock Adjustment Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "البيع: ${formatMoney(product.sellPrice)} ريال",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (product.buyPrice > 0) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "(الشراء: ${formatMoney(product.buyPrice)})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                        if (product.notes.isNotBlank()) {
                                            Text(product.notes, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                    }

                                    // Quick Stock Increment/Decrement
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        FilledTonalIconButton(
                                            onClick = { viewModel.adjustProductStock(product, -1) },
                                            modifier = Modifier.size(32.dp),
                                            enabled = product.stock > 0
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "-1", modifier = Modifier.size(16.dp))
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isOutOfStock) Color(0xFFFEE2E2) else if (isLowStock) Color(0xFFFEF3C7) else Color(0xFFE8F5E9),
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Text(
                                                "الكمية: ${product.stock}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isOutOfStock) DebtRed else if (isLowStock) Color(0xFFB45309) else CreditGreen
                                            )
                                        }

                                        FilledTonalIconButton(
                                            onClick = { viewModel.adjustProductStock(product, 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "+1", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            ProductDialog(
                product = productToEdit,
                onDismiss = {
                    showDialog = false
                    productToEdit = null
                },
                onSave = {
                    viewModel.saveProduct(it)
                    showDialog = false
                    productToEdit = null
                }
            )
        }

        if (productToDelete != null) {
            ConfirmDeleteDialog(
                title = "حذف صنف من المخزون",
                message = "هل أنت متأكد من حذف الصنف '${productToDelete?.name}' نهائياً؟",
                onConfirm = {
                    productToDelete?.let { viewModel.deleteProduct(it) }
                    productToDelete = null
                },
                onDismiss = { productToDelete = null }
            )
        }
    }
}
