package com.nk.app.data.repository

import com.nk.app.data.api.NkApi
import com.nk.app.domain.model.Category
import com.nk.app.domain.model.Product
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(private val api: NkApi) {

    suspend fun getProducts(
        query: String? = null,
        category: Int? = null,
        tag: String? = null,
        sort: String? = null
    ): List<Product> = api.getProducts(query, category, tag, sort)

    suspend fun getProduct(id: Int): Product = api.getProduct(id)

    suspend fun getCategories(): List<Category> = api.getCategories()
}
