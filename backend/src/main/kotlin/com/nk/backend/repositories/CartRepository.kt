package com.nk.backend.repositories

import com.nk.backend.db.CartItems
import com.nk.backend.models.CartItemDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CartRepository {

    fun findByUserId(userId: Int): List<CartItemDto> = transaction {
        CartItems.selectAll().where { CartItems.userId eq userId }.map {
            CartItemDto(
                productId = it[CartItems.productId],
                qty = it[CartItems.qty]
            )
        }
    }

    fun replaceAll(userId: Int, items: List<Pair<Int, Int>>) = transaction {
        CartItems.deleteWhere { CartItems.userId eq userId }
        items.forEach { (productId, qty) ->
            CartItems.insert {
                it[CartItems.userId] = userId
                it[CartItems.productId] = productId
                it[CartItems.qty] = qty
            }
        }
    }

    fun clear(userId: Int) = transaction {
        CartItems.deleteWhere { CartItems.userId eq userId }
    }
}
