package com.nk.app.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.repository.CartRepository
import com.nk.app.data.repository.ProductRepository
import com.nk.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItemFull(
    val product: Product,
    val qty: Int
)

data class CartState(
    val items: List<CartItemFull> = emptyList(),
    val promoCode: String = "",
    val promoDiscount: Int? = null,
    val bonusSpend: Int = 0,
    val userPoints: Int = 0,
    val isLoading: Boolean = true,
    val isOrdering: Boolean = false,
    val order: Order? = null,
    val error: String? = null
) {
    val subtotal: Int get() = items.sumOf { it.product.price * it.qty }
    val delivery: Int get() = if (subtotal >= 1000) 0 else 199
    val promoAmount: Int get() = if (promoDiscount != null) subtotal * promoDiscount / 100 else 0
    val total: Int get() = maxOf(0, subtotal + delivery - promoAmount - bonusSpend)
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepo: CartRepository,
    private val productRepo: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val cartItems = cartRepo.getCart()
                val products = productRepo.getProducts()
                val productMap = products.associateBy { it.id }
                val full = cartItems.mapNotNull { item ->
                    productMap[item.productId]?.let { CartItemFull(it, item.qty) }
                }
                _state.value = _state.value.copy(items = full, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun updateQty(productId: Int, qty: Int) {
        viewModelScope.launch {
            val items = _state.value.items.toMutableList()
            if (qty <= 0) {
                items.removeAll { it.product.id == productId }
            } else {
                val idx = items.indexOfFirst { it.product.id == productId }
                if (idx >= 0) items[idx] = items[idx].copy(qty = qty)
            }
            _state.value = _state.value.copy(items = items)
            try {
                cartRepo.updateCart(items.map { CartItem(it.product.id, it.qty) })
            } catch (_: Exception) {}
        }
    }

    fun setPromoCode(code: String) {
        _state.value = _state.value.copy(promoCode = code)
    }

    fun checkPromo() {
        viewModelScope.launch {
            try {
                val result = cartRepo.checkPromo(_state.value.promoCode)
                _state.value = _state.value.copy(
                    promoDiscount = if (result.valid) result.discountPercent else null
                )
            } catch (_: Exception) {}
        }
    }

    fun setBonusSpend(amount: Int) {
        val max = _state.value.subtotal * 30 / 100
        _state.value = _state.value.copy(bonusSpend = minOf(amount, max, _state.value.userPoints))
    }

    fun placeOrder() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isOrdering = true)
            try {
                val s = _state.value
                val order = cartRepo.createOrder(CreateOrderRequest(
                    items = s.items.map { OrderItemRequest(it.product.id, it.qty) },
                    promoCode = s.promoCode.ifBlank { null },
                    bonusSpend = s.bonusSpend
                ))
                _state.value = _state.value.copy(order = order, isOrdering = false, items = emptyList())
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isOrdering = false)
            }
        }
    }
}
