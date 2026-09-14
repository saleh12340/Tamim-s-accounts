package com.aistudio.tamimsaccounts

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "الرئيسية", Icons.Default.Home)
    object Accounts : Screen("accounts", "العملاء", Icons.Default.Person)
    object Products : Screen("products", "المخزون", Icons.Default.ShoppingCart)
    object More : Screen("more", "المزيد", Icons.Default.Menu)
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Dashboard,
        Screen.Accounts,
        Screen.Products,
        Screen.More
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Show bottom bar only on main screens
            if (currentRoute in items.map { it.route }) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel, navController) }
            composable(Screen.Accounts.route) { AccountsScreen(viewModel, navController) }
            composable(Screen.Products.route) { ProductsScreen(viewModel, navController) }
            composable(Screen.More.route) { MoreScreen(viewModel, navController) }
            composable("customer_ledger/{customerId}") { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId")?.toIntOrNull()
                if (customerId != null) {
                    CustomerLedgerScreen(customerId, viewModel, navController)
                }
            }
            composable("expenses") { ExpensesScreen(viewModel, navController) }
            composable("invoices") { InvoicesScreen(viewModel, navController) }
            composable("settings") { SettingsScreen(viewModel, navController) }
            composable("reports") { ReportsScreen(viewModel, navController) }
        }
    }
}
