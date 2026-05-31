package com.nk.backend.repositories

import com.nk.backend.db.Reviews
import com.nk.backend.models.ReviewDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object ReviewRepository {

    fun findByProductId(productId: Int): List<ReviewDto> = transaction {
        Reviews.selectAll().where { Reviews.productId eq productId }
            .orderBy(Reviews.createdAt to SortOrder.DESC)
            .map { it.toDto() }
    }

    fun create(productId: Int, userId: Int?, author: String?, rating: Int, text: String?): ReviewDto = transaction {
        val id = Reviews.insert {
            it[Reviews.productId] = productId
            it[Reviews.userId] = userId
            it[Reviews.author] = author
            it[Reviews.rating] = rating
            it[Reviews.text] = text
        } get Reviews.id

        Reviews.selectAll().where { Reviews.id eq id }.first().toDto()
    }

    private fun ResultRow.toDto() = ReviewDto(
        id = this[Reviews.id],
        productId = this[Reviews.productId],
        userId = this[Reviews.userId],
        author = this[Reviews.author],
        rating = this[Reviews.rating],
        text = this[Reviews.text],
        createdAt = this[Reviews.createdAt].toString()
    )
}
