package com.nk.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.nk.app.data.local.TokenStore
import com.nk.app.ui.navigation.Route
import com.nk.app.ui.screens.cart.CartScreen
import com.nk.app.ui.screens.catalog.CatalogScreen
import com.nk.app.ui.screens.diary.DiaryScreen
import com.nk.app.ui.screens.favorites.FavoritesScreen
import com.nk.app.ui.screens.onboarding.OnboardingScreen
import com.nk.app.ui.screens.orders.OrdersScreen
import com.nk.app.ui.screens.product.ProductScreen
import com.nk.app.ui.screens.profile.ProfileScreen
import com.nk.app.ui.screens.profile.AuthScreen
import com.nk.app.ui.screens.warehouse.AdminViewModel
import com.nk.app.ui.screens.warehouse.AdminOrdersScreen
import com.nk.app.ui.screens.warehouse.AdminProductsScreen
import com.nk.app.ui.screens.warehouse.AdminUsersScreen
import com.nk.app.ui.screens.warehouse.AdminCategoriesScreen
import com.nk.app.ui.theme.NkLabel3
import com.nk.app.ui.theme.NkTheme
import kotlinx.coroutines.runBlocking

data class TabItem(val route: String, val label: String, val icon: ImageVector)

// Табы для обычного пользователя
val userTabs = listOf(
    TabItem(Route.Catalog.route, "Каталог", Icons.Default.Restaurant),
    TabItem(Route.Favorites.route, "Избранное", Icons.Default.Favorite),
    TabItem(Route.Diary.route, "Дневник", Icons.Default.MenuBook),
    TabItem(Route.Cart.route, "Корзина", Icons.Default.ShoppingCart),
    TabItem(Route.Profile.route, "Профиль", Icons.Default.Person),
)

// Табы для админа
val adminTabs = listOf(
    TabItem(Route.AdminOrders.route, "Заказы", Icons.Default.Receipt),
    TabItem(Route.AdminProducts.route, "Товары", Icons.Default.Inventory),
    TabItem(Route.AdminUsers.route, "Юзеры", Icons.Default.People),
    TabItem(Route.AdminCategories.route, "Категории", Icons.Default.Category),
    TabItem(Route.Profile.route, "Профиль", Icons.Default.Person),
)

@Composable
fun NkAppRoot(tokenStore: TokenStore) {
    NkTheme {
        val hasAccount = remember { runBlocking { tokenStore.getToken() != null } }
        val startDestination = if (hasAccount) Route.Catalog.route else Route.Onboarding.route

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Определяем, admin ли текущий пользователь (по текущему маршруту)
        val isAdminUI = adminTabs.any { it.route == currentRoute }
        val isUserUI = userTabs.any { it.route == currentRoute }
        val tabs = if (isAdminUI) adminTabs else userTabs
        val showBottomBar = isAdminUI || isUserUI

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.88f),
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(84.dp)
                    ) {
                        tabs.forEach { tab ->
                            val selected = navBackStackEntry?.destination?.hierarchy
                                ?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(26.dp)) },
                                label = { Text(tab.label, fontSize = 10.sp, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = NkLabel3,
                                    unselectedTextColor = NkLabel3,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding).background(MaterialTheme.colorScheme.background)
            ) {
                // Обычные табы
                composable(Route.Catalog.route) {
                    CatalogScreen(onProductClick = { id -> navController.navigate(Route.ProductDetail.create(id)) })
                }
                composable(Route.Favorites.route) {
                    FavoritesScreen(onProductClick = { id -> navController.navigate(Route.ProductDetail.create(id)) })
                }
                composable(Route.Diary.route) { DiaryScreen() }
                composable(Route.Cart.route) {
                    CartScreen(onOrderSuccess = { navController.navigate(Route.Orders.route) })
                }
                composable(Route.Profile.route) {
                    ProfileScreen(
                        onNavigateToAuth = { navController.navigate(Route.Auth.route) },
                        onNavigateToOrders = { navController.navigate(Route.Orders.route) },
                        onNavigateToOnboarding = { navController.navigate(Route.Onboarding.route) },
                        onNavigateToWarehouse = {
                            // Переключаемся на админ-табы
                            navController.navigate(Route.AdminOrders.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // Админ-табы (shared ViewModel)
                composable(Route.AdminOrders.route) {
                    val vm: AdminViewModel = hiltViewModel(navController.getBackStackEntry(Route.AdminOrders.route))
                    AdminOrdersScreen(vm)
                }
                composable(Route.AdminProducts.route) {
                    val vm: AdminViewModel = hiltViewModel()
                    AdminProductsScreen(vm)
                }
                composable(Route.AdminUsers.route) {
                    val vm: AdminViewModel = hiltViewModel()
                    AdminUsersScreen(vm)
                }
                composable(Route.AdminCategories.route) {
                    val vm: AdminViewModel = hiltViewModel()
                    AdminCategoriesScreen(vm)
                }

                // Вложенные экраны
                composable(Route.ProductDetail.route, arguments = listOf(navArgument("id") { type = NavType.IntType })) { backStackEntry ->
                    ProductScreen(productId = backStackEntry.arguments?.getInt("id") ?: 0, onBack = { navController.popBackStack() })
                }
                composable(Route.Orders.route) {
                    OrdersScreen(onBack = { navController.popBackStack() })
                }
                composable(Route.Auth.route) {
                    AuthScreen(onSuccess = { navController.popBackStack() }, onBack = { navController.popBackStack() })
                }
                composable(Route.Onboarding.route) {
                    OnboardingScreen(onComplete = {
                        navController.navigate(Route.Catalog.route) {
                            popUpTo(Route.Onboarding.route) { inclusive = true }
                        }
                    })
                }
            }
        }
    }
}
