package com.nk.backend.repositories

import com.nk.backend.db.*
import com.nk.backend.models.ProductDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object ProductRepository {

    fun findAll(
        query: String? = null,
        category: String? = null,
        tag: String? = null,
        sort: String? = null
    ): List<ProductDto> = transaction {
        val baseQuery = Products
            .join(Categories, JoinType.LEFT, Products.categoryId, Categories.id)
            .selectAll()

        // Фильтр по поиску
        if (!query.isNullOrBlank()) {
            baseQuery.andWhere {
                Products.name.lowerCase() like "%${query.lowercase()}%"
            }
        }

        // Фильтр по категории
        if (!category.isNullOrBlank()) {
            baseQuery.andWhere { Categories.name eq category }
        }

        // Фильтр по тегу
        if (!tag.isNullOrBlank()) {
            val productIdsWithTag = ProductTags
                .selectAll().where { ProductTags.tag eq tag }
                .map { it[ProductTags.productId] }
            baseQuery.andWhere { Products.id inList productIdsWithTag }
        }

        // Сортировка
        when (sort) {
            "priceA" -> baseQuery.orderBy(Products.price to SortOrder.ASC)
            "priceD" -> baseQuery.orderBy(Products.price to SortOrder.DESC)
            "kcalA" -> baseQuery.orderBy(Products.kcal to SortOrder.ASC)
            else -> baseQuery.orderBy(Products.id to SortOrder.ASC)
        }

        val productRows = baseQuery.toList()

        // Загружаем теги для всех продуктов разом
        val allTags = ProductTags.selectAll().toList()
            .groupBy({ it[ProductTags.productId] }, { it[ProductTags.tag] })

        productRows.map { row ->
            row.toProductDto(allTags[row[Products.id]] ?: emptyList())
        }
    }

    fun findById(id: Int): ProductDto? = transaction {
        val row = Products
            .join(Categories, JoinType.LEFT, Products.categoryId, Categories.id)
            .selectAll().where { Products.id eq id }
            .firstOrNull() ?: return@transaction null

        val tags = ProductTags.selectAll().where { ProductTags.productId eq id }
            .map { it[ProductTags.tag] }

        // Отзывы и рейтинг
        val reviews = Reviews.selectAll().where { Reviews.productId eq id }.toList()
        val avgRating = if (reviews.isNotEmpty()) {
            reviews.map { it[Reviews.rating] }.average()
        } else null

        row.toProductDto(tags).copy(
            avgRating = avgRating?.let { Math.round(it * 10) / 10.0 },
            reviewCount = reviews.size
        )
    }

    fun existsById(id: Int): Boolean = transaction {
        Products.selectAll().where { Products.id eq id }.count() > 0
    }

    fun getPriceById(id: Int): Int? = transaction {
        Products.selectAll().where { Products.id eq id }
            .firstOrNull()?.get(Products.price)
    }

    fun count(): Int = transaction {
        Products.selectAll().count().toInt()
    }

    private fun ResultRow.toProductDto(tags: List<String>) = ProductDto(
        id = this[Products.id],
        categoryId = this[Products.categoryId],
        categoryName = this.getOrNull(Categories.name),
        name = this[Products.name],
        kcal = this[Products.kcal],
        protein = this[Products.protein],
        fat = this[Products.fat],
        carb = this[Products.carb],
        grams = this[Products.grams],
        price = this[Products.price],
        sostav = this[Products.sostav],
        benefit = this[Products.benefit],
        isHit = this[Products.isHit],
        isNovelty = this[Products.isNovelty],
        imageUrl = this[Products.imageUrl],
        tags = tags
    )
}
