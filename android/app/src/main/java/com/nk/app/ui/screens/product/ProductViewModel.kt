package com.nk.app.ui.screens.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.repository.CartRepository
import com.nk.app.data.repository.DiaryRepository
import com.nk.app.data.repository.ProductRepository
import com.nk.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductState(
    val product: Product? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val addedToCart: Boolean = false,
    val addedToDiary: Boolean = false
)

@HiltViewModel
class ProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepo: ProductRepository,
    private val cartRepo: CartRepository,
    private val diaryRepo: DiaryRepository
) : ViewModel() {

    private val productId: Int = savedStateHandle["id"] ?: 0
    private val _state = MutableStateFlow(ProductState())
    val state: StateFlow<ProductState> = _state

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val product = productRepo.getProduct(productId)
                val favs = try { diaryRepo.getFavorites() } catch (_: Exception) { emptyList() }
                _state.value = ProductState(
                    product = product,
                    isFavorite = productId in favs,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = ProductState(error = e.message, isLoading = false)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val favs = diaryRepo.getFavorites().toMutableList()
                if (productId in favs) favs.remove(productId) else favs.add(productId)
                diaryRepo.updateFavorites(favs)
                _state.value = _state.value.copy(isFavorite = productId in favs)
            } catch (_: Exception) {}
        }
    }

    fun addToCart() {
        viewModelScope.launch {
            try {
                val cart = cartRepo.getCart().toMutableList()
                val existing = cart.find { it.productId == productId }
                if (existing != null) {
                    cart[cart.indexOf(existing)] = existing.copy(qty = existing.qty + 1)
                } else {
                    cart.add(CartItem(productId, 1))
                }
                cartRepo.updateCart(cart)
                _state.value = _state.value.copy(addedToCart = true)
            } catch (_: Exception) {}
        }
    }

    fun addToDiary(meal: String) {
        viewModelScope.launch {
            try {
                diaryRepo.addEntry(CreateDiaryEntry(
                    productId = productId,
                    qty = 1,
                    meal = meal
                ))
                _state.value = _state.value.copy(addedToDiary = true)
            } catch (_: Exception) {}
        }
    }
}
