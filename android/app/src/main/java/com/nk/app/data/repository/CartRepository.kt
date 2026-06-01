package com.nk.app.data.repository

import com.nk.app.data.api.NkApi
import com.nk.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(private val api: NkApi) {

    suspend fun getCart(): List<CartItem> = api.getCart()

    suspend fun updateCart(items: List<CartItem>): List<CartItem> {
        return api.updateCart(CartRequest(items.map { CartItemRequest(it.productId, it.qty) }))
    }

    suspend fun createOrder(req: CreateOrderRequest): Order = api.createOrder(req)

    suspend fun getOrders(): List<Order> = api.getOrders()

    suspend fun checkPromo(code: String): PromoCheck = api.checkPromo(code)
}
