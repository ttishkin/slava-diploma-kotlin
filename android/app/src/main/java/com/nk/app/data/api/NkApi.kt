package com.nk.app.data.api

import com.nk.app.domain.model.*
import retrofit2.http.*

interface NkApi {

    // === Авторизация ===
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun getProfile(): User

    // === Каталог ===
    @GET("api/products")
    suspend fun getProducts(
        @Query("q") query: String? = null,
        @Query("category") category: Int? = null,
        @Query("tag") tag: String? = null,
        @Query("sort") sort: String? = null
    ): List<Product>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Product

    @GET("api/categories")
    suspend fun getCategories(): List<Category>

    // === Корзина ===
    @GET("api/cart")
    suspend fun getCart(): List<CartItem>

    @PUT("api/cart")
    suspend fun updateCart(@Body req: CartRequest): List<CartItem>

    // === Избранное ===
    @GET("api/favorites")
    suspend fun getFavorites(): FavoritesResponse

    @PUT("api/favorites")
    suspend fun updateFavorites(@Body req: FavoritesRequest): FavoritesResponse

    // === Заказы ===
    @POST("api/orders")
    suspend fun createOrder(@Body req: CreateOrderRequest): Order

    @GET("api/orders")
    suspend fun getOrders(): List<Order>

    @GET("api/orders/{id}")
    suspend fun getOrder(@Path("id") id: Int): Order

    // === Отзывы ===
    @POST("api/reviews")
    suspend fun createReview(@Body req: CreateReviewRequest): Review

    @GET("api/products/{id}/reviews")
    suspend fun getReviews(@Path("id") productId: Int): List<Review>

    // === Бонусы ===
    @GET("api/bonuses")
    suspend fun getBonuses(): BonusResponse

    // === Дневник ===
    @GET("api/diary")
    suspend fun getDiary(@Query("day") day: String): List<DiaryEntry>

    @POST("api/diary")
    suspend fun addDiaryEntry(@Body req: CreateDiaryEntry): DiaryEntry

    @PATCH("api/diary/{id}")
    suspend fun updateDiaryEntry(@Path("id") id: Int, @Body req: UpdateDiaryEntry): DiaryEntry

    @DELETE("api/diary/{id}")
    suspend fun deleteDiaryEntry(@Path("id") id: Int): MessageResponse

    // === Адреса ===
    @GET("api/addresses")
    suspend fun getAddresses(): List<Address>

    @POST("api/addresses")
    suspend fun createAddress(@Body req: CreateAddress): Address

    @DELETE("api/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: Int): MessageResponse

    // === Прочее ===
    @GET("api/pickup-points")
    suspend fun getPickupPoints(): List<PickupPoint>

    @GET("api/promo/{code}")
    suspend fun checkPromo(@Path("code") code: String): PromoCheck

    @GET("api/notifications")
    suspend fun getNotifications(): List<Notification>
}
