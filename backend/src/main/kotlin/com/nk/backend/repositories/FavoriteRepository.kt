package com.nk.backend.repositories

import com.nk.backend.db.Favorites
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object FavoriteRepository {

    fun findByUserId(userId: Int): List<Int> = transaction {
        Favorites.selectAll().where { Favorites.userId eq userId }
            .map { it[Favorites.productId] }
    }

    fun replaceAll(userId: Int, productIds: List<Int>) = transaction {
        Favorites.deleteWhere { Favorites.userId eq userId }
        productIds.forEach { productId ->
            Favorites.insert {
                it[Favorites.userId] = userId
                it[Favorites.productId] = productId
            }
        }
    }
}
