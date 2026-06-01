package com.nk.app.domain.model

import kotlinx.serialization.Serializable

// === Авторизация ===

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String? = null,
    val sex: String? = null,
    val age: Int? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val activity: String? = null,
    val goal: String? = null
)

@Serializable
data class AuthResponse(val token: String, val user: User)

@Serializable
data class User(
    val id: Int,
    val email: String,
    val name: String? = null,
    val sex: String? = null,
    val age: Int? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val activity: Double? = null,
    val goal: String? = null,
    val kcalNorm: Int? = null,
    val points: Int = 0,
    val role: String = "user"
)

// === Продукты ===

@Serializable
data class Product(
    val id: Int,
    val categoryId: Int,
    val categoryName: String? = null,
    val name: String,
    val kcal: Int,
    val protein: Double,
    val fat: Double,
    val carb: Double,
    val grams: Int,
    val price: Int,
    val sostav: String? = null,
    val benefit: String? = null,
    val isHit: Boolean = false,
    val isNovelty: Boolean = false,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val avgRating: Double? = null,
    val reviewCount: Int? = null,
    val reviews: List<Review>? = null
)

@Serializable
data class Category(
    val id: Int,
    val name: String,
    val color: String,
    val glyph: String
)

// === Корзина ===

@Serializable
data class CartRequest(val items: List<CartItemRequest>)

@Serializable
data class CartItemRequest(val productId: Int, val qty: Int)

@Serializable
data class CartItem(val productId: Int, val qty: Int)

// === Избранное ===

@Serializable
data class FavoritesRequest(val productIds: List<Int>)

@Serializable
data class FavoritesResponse(val productIds: List<Int>)

// === Заказы ===

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val promoCode: String? = null,
    val bonusSpend: Int = 0,
    val addressId: Int? = null
)

@Serializable
data class OrderItemRequest(val productId: Int, val qty: Int)

@Serializable
data class Order(
    val id: Int,
    val userId: Int? = null,
    val no: String,
    val total: Int,
    val delivery: Int,
    val bonusEarned: Int,
    val bonusSpent: Int,
    val promoCode: String? = null,
    val status: String,
    val addressId: Int? = null,
    val createdAt: String,
    val items: List<OrderItem> = emptyList(),
    val userEmail: String? = null
)

@Serializable
data class OrderItem(
    val id: Int,
    val productId: Int,
    val productName: String? = null,
    val qty: Int,
    val price: Int
)

// === Отзывы ===

@Serializable
data class CreateReviewRequest(
    val productId: Int,
    val rating: Int,
    val text: String? = null,
    val author: String? = null
)

@Serializable
data class Review(
    val id: Int,
    val productId: Int,
    val userId: Int? = null,
    val author: String? = null,
    val rating: Int,
    val text: String? = null,
    val createdAt: String
)

// === Бонусы ===

@Serializable
data class BonusResponse(
    val balance: Int,
    val transactions: List<BonusTransaction>
)

@Serializable
data class BonusTransaction(
    val orderId: Int? = null,
    val amount: Int,
    val type: String,
    val createdAt: String
)

// === Дневник ===

@Serializable
data class CreateDiaryEntry(
    val productId: Int? = null,
    val name: String? = null,
    val qty: Int = 1,
    val grams: Int? = null,
    val kcal: Int? = null,
    val meal: String = "Перекус",
    val day: String? = null
)

@Serializable
data class UpdateDiaryEntry(val qty: Int)

@Serializable
data class DiaryEntry(
    val id: Int,
    val productId: Int? = null,
    val name: String,
    val qty: Int,
    val grams: Int,
    val kcal: Int,
    val meal: String,
    val day: String,
    val createdAt: String
)

// === Адреса ===

@Serializable
data class CreateAddress(
    val address: String,
    val label: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

@Serializable
data class Address(
    val id: Int,
    val label: String? = null,
    val address: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val isPickup: Boolean = false
)

@Serializable
data class PickupPoint(
    val id: Int,
    val name: String,
    val address: String,
    val lat: Double? = null,
    val lng: Double? = null
)

// === Промо ===

@Serializable
data class PromoCheck(
    val valid: Boolean,
    val code: String? = null,
    val discountPercent: Int? = null
)

// === Уведомления ===

@Serializable
data class Notification(
    val id: Int,
    val orderId: Int? = null,
    val email: String? = null,
    val message: String,
    val createdAt: String
)

// === Общие ===

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class MessageResponse(val message: String)
