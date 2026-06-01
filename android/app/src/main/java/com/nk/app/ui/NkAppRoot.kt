package com.nk.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
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
import com.nk.app.ui.theme.NkTheme

data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val tabs = listOf(
    TabItem(Route.Catalog.route, "Каталог", Icons.Default.Restaurant),
    TabItem(Route.Favorites.route, "Избранное", Icons.Default.Favorite),
    TabItem(Route.Diary.route, "Дневник", Icons.Default.MenuBook),
    TabItem(Route.Cart.route, "Корзина", Icons.Default.ShoppingCart),
    TabItem(Route.Profile.route, "Профиль", Icons.Default.Person),
)

@Composable
fun NkAppRoot() {
    NkTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Табы видны только на главных экранах
        val showBottomBar = tabs.any { it.route == currentRoute }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        tabs.forEach { tab ->
                            val selected = navBackStackEntry?.destination?.hierarchy
                                ?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Route.Catalog.route,
                modifier = Modifier.padding(padding)
            ) {
                // Главные табы
                composable(Route.Catalog.route) {
                    CatalogScreen(
                        onProductClick = { id ->
                            navController.navigate(Route.ProductDetail.create(id))
                        }
                    )
                }
                composable(Route.Favorites.route) {
                    FavoritesScreen(
                        onProductClick = { id ->
                            navController.navigate(Route.ProductDetail.create(id))
                        }
                    )
                }
                composable(Route.Diary.route) {
                    DiaryScreen()
                }
                composable(Route.Cart.route) {
                    CartScreen(
                        onOrderSuccess = {
                            navController.navigate(Route.Orders.route)
                        }
                    )
                }
                composable(Route.Profile.route) {
                    ProfileScreen(
                        onNavigateToAuth = { navController.navigate(Route.Auth.route) },
                        onNavigateToOrders = { navController.navigate(Route.Orders.route) },
                        onNavigateToOnboarding = { navController.navigate(Route.Onboarding.route) }
                    )
                }

                // Вложенные экраны
                composable(
                    Route.ProductDetail.route,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getInt("id") ?: 0
                    ProductScreen(
                        productId = productId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Route.Orders.route) {
                    OrdersScreen(onBack = { navController.popBackStack() })
                }
                composable(Route.Auth.route) {
                    AuthScreen(
                        onSuccess = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Route.Onboarding.route) {
                    OnboardingScreen(
                        onComplete = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
