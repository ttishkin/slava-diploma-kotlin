package com.nk.backend.models

import kotlinx.serialization.Serializable

// === DTO для запросов ===

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String? = null,
    val sex: String? = null,
    val age: Int? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val activity: Double? = null,
    val goal: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val name: String?,
    val sex: String?,
    val age: Int?,
    val height: Int?,
    val weight: Int?,
    val activity: Double?,
    val goal: String?,
    val kcalNorm: Int?,
    val points: Int,
    val role: String
)

// === Продукты ===

@Serializable
data class ProductDto(
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
    val reviews: List<ReviewDto>? = null
)

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String,
    val color: String,
    val glyph: String
)

// === Заказы ===

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val promoCode: String? = null,
    val bonusSpend: Int = 0,
    val addressId: Int? = null
)

@Serializable
data class OrderItemRequest(
    val id: Int,
    val qty: Int
)

@Serializable
data class OrderDto(
    val id: Int,
    val no: String,
    val total: Int,
    val delivery: Int,
    val bonusEarned: Int,
    val bonusSpent: Int,
    val promoCode: String?,
    val status: String,
    val addressId: Int?,
    val createdAt: String,
    val items: List<OrderItemDto> = emptyList(),
    val userEmail: String? = null
)

@Serializable
data class OrderItemDto(
    val id: Int,
    val productId: Int,
    val productName: String? = null,
    val qty: Int,
    val price: Int
)

@Serializable
data class UpdateStatusRequest(
    val status: String
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
data class ReviewDto(
    val id: Int,
    val productId: Int,
    val userId: Int?,
    val author: String?,
    val rating: Int,
    val text: String?,
    val createdAt: String
)

// === Бонусы ===

@Serializable
data class BonusResponse(
    val balance: Int,
    val transactions: List<BonusTransactionDto>
)

@Serializable
data class BonusTransactionDto(
    val orderId: Int?,
    val amount: Int,
    val type: String,
    val createdAt: String
)

// === Дневник питания ===

@Serializable
data class CreateDiaryEntryRequest(
    val productId: Int? = null,
    val name: String? = null,
    val qty: Int = 1,
    val grams: Int? = null,
    val kcal: Int? = null,
    val meal: String = "Перекус"
)

@Serializable
data class UpdateDiaryEntryRequest(
    val qty: Int
)

@Serializable
data class DiaryEntryDto(
    val id: Int,
    val productId: Int?,
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
data class CreateAddressRequest(
    val address: String,
    val label: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

@Serializable
data class AddressDto(
    val id: Int,
    val label: String?,
    val address: String,
    val lat: Double?,
    val lng: Double?,
    val isPickup: Boolean
)

// === Пункты самовывоза ===

@Serializable
data class PickupPointDto(
    val id: Int,
    val name: String,
    val address: String,
    val lat: Double?,
    val lng: Double?
)

// === Корзина ===

@Serializable
data class CartRequest(
    val items: List<CartItemRequest>
)

@Serializable
data class CartItemRequest(
    val id: Int,
    val qty: Int
)

@Serializable
data class CartItemDto(
    val productId: Int,
    val qty: Int
)

// === Избранное ===

@Serializable
data class FavoritesRequest(
    val ids: List<Int>
)

// === Промокод ===

@Serializable
data class PromoCheckResponse(
    val valid: Boolean,
    val code: String? = null,
    val discountPercent: Int? = null
)

// === Уведомления ===

@Serializable
data class NotificationDto(
    val id: Int,
    val orderId: Int?,
    val email: String?,
    val message: String,
    val createdAt: String
)

// === Админ: статистика ===

@Serializable
data class AdminStatsDto(
    val orders: Int,
    val revenue: Int,
    val users: Int,
    val products: Int,
    val byStatus: List<StatusCountDto>
)

@Serializable
data class StatusCountDto(
    val status: String,
    val count: Int
)

@Serializable
data class AdminUserDto(
    val id: Int,
    val email: String,
    val name: String?,
    val role: String,
    val points: Int,
    val createdAt: String,
    val orderCount: Int = 0
)

@Serializable
data class DailySaleDto(
    val day: String,
    val orders: Int,
    val revenue: Int
)

// === Общие ===

@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class MessageResponse(
    val message: String
)
