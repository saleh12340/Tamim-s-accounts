package com.aistudio.tamimsaccounts

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "الرئيسية", Icons.Default.Home)
    object Accounts : Screen("accounts", "الحسابات", Icons.Default.Person)
    object Products : Screen("products", "المخزون", Icons.Default.ShoppingCart)
    object Reports : Screen("reports", "التقارير", Icons.AutoMirrored.Filled.List)
    object More : Screen("more", "المزيد", Icons.Default.MoreHoriz)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val bottom = listOf(Screen.Dashboard, Screen.Accounts, Screen.Products, Screen.Reports, Screen.More)

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        ModalDrawerSheet {
            NavigationDrawerItem(selected = route == "dashboard", onClick = { scope.launch { drawerState.close() }; navController.navigate("dashboard") }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("الرئيسية") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
            NavigationDrawerItem(selected = route == "accounts", onClick = { scope.launch { drawerState.close() }; navController.navigate("accounts") }, icon = { Icon(Icons.Default.People, null) }, label = { Text("حسابات العملاء") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
            NavigationDrawerItem(selected = route == "products", onClick = { scope.launch { drawerState.close() }; navController.navigate("products") }, icon = { Icon(Icons.Default.Inventory2, null) }, label = { Text("المخزون") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
            NavigationDrawerItem(selected = route == "invoices", onClick = { scope.launch { drawerState.close() }; navController.navigate("invoices") }, icon = { Icon(Icons.Default.ReceiptLong, null) }, label = { Text("الفواتير") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
            NavigationDrawerItem(selected = route == "expenses", onClick = { scope.launch { drawerState.close() }; navController.navigate("expenses") }, icon = { Icon(Icons.Default.Payments, null) }, label = { Text("المصروفات") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
            NavigationDrawerItem(selected = route == "reports", onClick = { scope.launch { drawerState.close() }; navController.navigate("reports") }, icon = { Icon(Icons.AutoMirrored.Filled.List, null) }, label = { Text("التقارير") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
            HorizontalDivider()
            NavigationDrawerItem(selected = route == "settings", onClick = { scope.launch { drawerState.close() }; navController.navigate("settings") }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("الإعدادات") }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
        }
    }) {
        Scaffold(bottomBar = {
            if (route in bottom.map { it.route }) NavigationBar { bottom.forEach { item -> NavigationBarItem(selected = route == item.route, onClick = { navController.navigate(item.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }, icon = { Icon(item.icon, item.title) }, label = { Text(item.title) }) } }
        }) { inner ->
            NavHost(navController, startDestination = Screen.Dashboard.route, modifier = Modifier.padding(inner)) {
                composable("dashboard") { DashboardScreen(viewModel, navController) { scope.launch { drawerState.open() } } }
                composable("accounts") { AccountsScreen(viewModel, navController) { scope.launch { drawerState.open() } } }
                composable("products") { ProductsScreen(viewModel, navController) }
                composable("reports") { ReportsScreen(viewModel, navController) }
                composable("more") { MoreScreen(viewModel, navController) }
                composable("customer_ledger/{customerId}") { e -> e.arguments?.getString("customerId")?.toIntOrNull()?.let { CustomerLedgerScreen(it, viewModel, navController) { scope.launch { drawerState.open() } } } }
                composable("expenses") { ExpensesScreen(viewModel, navController) }
                composable("invoices") { InvoicesScreen(viewModel, navController) }
                composable("settings") { SettingsScreen(viewModel, navController) }
            }
        }
    }
}
