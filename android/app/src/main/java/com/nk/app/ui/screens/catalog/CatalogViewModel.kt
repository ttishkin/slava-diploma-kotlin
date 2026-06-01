package com.nk.app.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nk.app.data.repository.ProductRepository
import com.nk.app.domain.model.Category
import com.nk.app.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val query: String = "",
    val selectedCategory: Int? = null,
    val selectedTag: String? = null,
    val sort: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repo: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = _state

    init {
        loadCategories()
        loadProducts()
    }

    fun search(query: String) {
        _state.value = _state.value.copy(query = query)
        loadProducts()
    }

    fun selectCategory(id: Int?) {
        _state.value = _state.value.copy(selectedCategory = id)
        loadProducts()
    }

    fun selectTag(tag: String?) {
        _state.value = _state.value.copy(
            selectedTag = if (_state.value.selectedTag == tag) null else tag
        )
        loadProducts()
    }

    fun setSort(sort: String?) {
        _state.value = _state.value.copy(sort = sort)
        loadProducts()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = repo.getCategories()
                _state.value = _state.value.copy(categories = categories)
            } catch (_: Exception) {}
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val s = _state.value
                val products = repo.getProducts(
                    query = s.query.ifBlank { null },
                    category = s.selectedCategory,
                    tag = s.selectedTag,
                    sort = s.sort
                )
                _state.value = _state.value.copy(products = products, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Ошибка загрузки",
                    isLoading = false
                )
            }
        }
    }
}
