package com.nk.app.ui.navigation

// Маршруты навигации
sealed class Route(val route: String) {
    // Онбординг
    data object Onboarding : Route("onboarding")

    // Главные табы
    data object Catalog : Route("catalog")
    data object Favorites : Route("favorites")
    data object Diary : Route("diary")
    data object Cart : Route("cart")
    data object Profile : Route("profile")

    // Вложенные экраны
    data object ProductDetail : Route("product/{id}") {
        fun create(id: Int) = "product/$id"
    }
    data object Orders : Route("orders")
    data object Auth : Route("auth")
}
