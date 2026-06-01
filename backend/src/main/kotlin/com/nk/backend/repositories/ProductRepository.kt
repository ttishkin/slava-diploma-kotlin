package com.nk.backend.repositories

import com.nk.backend.db.*
import com.nk.backend.models.ProductDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

        // Фильтр по категории (принимаем ID или имя)
        if (!category.isNullOrBlank()) {
            val categoryInt = category.toIntOrNull()
            if (categoryInt != null) {
                baseQuery.andWhere { Products.categoryId eq categoryInt }
            } else {
                baseQuery.andWhere { Categories.name eq category }
            }
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
            "priceA", "price_asc" -> baseQuery.orderBy(Products.price to SortOrder.ASC)
            "priceD", "price_desc" -> baseQuery.orderBy(Products.price to SortOrder.DESC)
            "kcalA", "kcal_asc" -> baseQuery.orderBy(Products.kcal to SortOrder.ASC)
            "kcalD", "kcal_desc" -> baseQuery.orderBy(Products.kcal to SortOrder.DESC)
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

    fun create(
        name: String, categoryId: Int, kcal: Int, protein: Double, fat: Double, carb: Double,
        grams: Int, price: Int, sostav: String?, benefit: String?,
        isHit: Boolean, isNovelty: Boolean, imageUrl: String?, tags: List<String>
    ): Int = transaction {
        val productId = Products.insert {
            it[Products.name] = name
            it[Products.categoryId] = categoryId
            it[Products.kcal] = kcal
            it[Products.protein] = protein
            it[Products.fat] = fat
            it[Products.carb] = carb
            it[Products.grams] = grams
            it[Products.price] = price
            it[Products.sostav] = sostav
            it[Products.benefit] = benefit
            it[Products.isHit] = isHit
            it[Products.isNovelty] = isNovelty
            it[Products.imageUrl] = imageUrl
        } get Products.id

        tags.forEach { tag ->
            ProductTags.insert {
                it[ProductTags.productId] = productId
                it[ProductTags.tag] = tag
            }
        }

        productId
    }

    fun update(
        id: Int, name: String?, categoryId: Int?, kcal: Int?, protein: Double?, fat: Double?, carb: Double?,
        grams: Int?, price: Int?, sostav: String?, benefit: String?,
        isHit: Boolean?, isNovelty: Boolean?, imageUrl: String?, tags: List<String>?
    ) = transaction {
        Products.update({ Products.id eq id }) {
            if (name != null) it[Products.name] = name
            if (categoryId != null) it[Products.categoryId] = categoryId
            if (kcal != null) it[Products.kcal] = kcal
            if (protein != null) it[Products.protein] = protein
            if (fat != null) it[Products.fat] = fat
            if (carb != null) it[Products.carb] = carb
            if (grams != null) it[Products.grams] = grams
            if (price != null) it[Products.price] = price
            if (sostav != null) it[Products.sostav] = sostav
            if (benefit != null) it[Products.benefit] = benefit
            if (isHit != null) it[Products.isHit] = isHit
            if (isNovelty != null) it[Products.isNovelty] = isNovelty
            if (imageUrl != null) it[Products.imageUrl] = imageUrl
        }

        // Заменяем теги, если переданы
        if (tags != null) {
            ProductTags.deleteWhere { ProductTags.productId eq id }
            tags.forEach { tag ->
                ProductTags.insert {
                    it[ProductTags.productId] = id
                    it[ProductTags.tag] = tag
                }
            }
        }
    }

    fun delete(id: Int) = transaction {
        // Удаляем связанные данные
        ProductTags.deleteWhere { ProductTags.productId eq id }
        Reviews.deleteWhere { Reviews.productId eq id }
        Products.deleteWhere { Products.id eq id }
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
