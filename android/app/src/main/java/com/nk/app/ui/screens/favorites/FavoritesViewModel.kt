package com.nk.app.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.repository.DiaryRepository
import com.nk.app.data.repository.ProductRepository
import com.nk.app.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val diaryRepo: DiaryRepository,
    private val productRepo: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val favIds = diaryRepo.getFavorites()
                if (favIds.isEmpty()) {
                    _state.value = FavoritesState(isLoading = false)
                    return@launch
                }
                val allProducts = productRepo.getProducts()
                val favProducts = allProducts.filter { it.id in favIds }
                _state.value = FavoritesState(products = favProducts, isLoading = false)
            } catch (e: Exception) {
                _state.value = FavoritesState(error = e.message, isLoading = false)
            }
        }
    }

    fun removeFavorite(productId: Int) {
        viewModelScope.launch {
            try {
                val favIds = diaryRepo.getFavorites().toMutableList()
                favIds.remove(productId)
                diaryRepo.updateFavorites(favIds)
                _state.value = _state.value.copy(
                    products = _state.value.products.filter { it.id != productId }
                )
            } catch (_: Exception) {}
        }
    }
}
