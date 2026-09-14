package com.aistudio.tamimsaccounts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "الرئيسية", Icons.Default.Home)
    object Accounts : Screen("accounts", "العملاء", Icons.Default.Person)
    object Products : Screen("products", "المخزون", Icons.Default.ShoppingCart)
    object Reports : Screen("reports", "التقارير", Icons.AutoMirrored.Filled.List)
    object Settings : Screen("settings", "الإعدادات", Icons.Default.Settings)
    object More : Screen("more", "المزيد", Icons.Default.Menu)
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val menuItems = listOf(
        "إضافة مبلغ" to Icons.Default.Add,
        "تقرير- إجمالي المبالغ" to Icons.Default.Assessment,
        "تقرير- تفاصيل كل المبالغ" to Icons.Default.Assignment,
        "تقرير- إجمالي المبالغ شهرياً" to Icons.Default.DateRange,
        "تقرير- إجمالي التصنيفات" to Icons.Default.Category,
        "تقرير- حركة الحسابات" to Icons.Default.SyncAlt,
        "التكرار التلقائي" to Icons.Default.Repeat,
        "حفظ نسخة إحتياطية" to Icons.Default.CloudUpload,
        "إسترجاع قاعدة البيانات" to Icons.Default.CloudDownload,
        "جوجل درايف" to Icons.Default.SdStorage,
        "إعدادات" to Icons.Default.Settings,
        "للتواصل والدعم" to Icons.Default.Phone,
        "حول البرنامج" to Icons.Default.Info,
        "مشاركة البرنامج" to Icons.Default.Share,
        "خروج" to Icons.Default.ExitToApp
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                menuItems.forEach { (title, icon) ->
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(title) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            when (title) {
                                "إعدادات" -> navController.navigate("settings")
                                "الرئيسية" -> navController.navigate("dashboard")
                                "العملاء" -> navController.navigate("accounts")
                                "المزيد" -> navController.navigate("more")
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) { DashboardScreen(viewModel, navController, onOpenDrawer = { scope.launch { drawerState.open() } }) }
                composable(Screen.Accounts.route) { AccountsScreen(viewModel, navController, onOpenDrawer = { scope.launch { drawerState.open() } }) }
                composable(Screen.Products.route) { ProductsScreen(viewModel, navController) }
                composable(Screen.More.route) { MoreScreen(viewModel, navController) }
                composable("customer_ledger/{customerId}") { backStackEntry ->
                    val customerId = backStackEntry.arguments?.getString("customerId")?.toIntOrNull()
                    if (customerId != null) {
                        CustomerLedgerScreen(customerId, viewModel, navController, onOpenDrawer = { scope.launch { drawerState.open() } })
                    }
                }
                composable("expenses") { ExpensesScreen(viewModel, navController) }
                composable("invoices") { InvoicesScreen(viewModel, navController) }
                composable("settings") { SettingsScreen(viewModel, navController) }
                composable("reports") { ReportsScreen(viewModel, navController) }
            }
        }
    }
}
