package com.nk.backend.repositories

import com.nk.backend.db.BonusTransactions
import com.nk.backend.models.BonusTransactionDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BonusRepository {

    fun findByUserId(userId: Int): List<BonusTransactionDto> = transaction {
        BonusTransactions.selectAll().where { BonusTransactions.userId eq userId }
            .orderBy(BonusTransactions.createdAt to SortOrder.DESC)
            .map {
                BonusTransactionDto(
                    orderId = it[BonusTransactions.orderId],
                    amount = it[BonusTransactions.amount],
                    type = it[BonusTransactions.type],
                    createdAt = it[BonusTransactions.createdAt].toString()
                )
            }
    }

    fun create(userId: Int, orderId: Int?, amount: Int, type: String) = transaction {
        BonusTransactions.insert {
            it[BonusTransactions.userId] = userId
            it[BonusTransactions.orderId] = orderId
            it[BonusTransactions.amount] = amount
            it[BonusTransactions.type] = type
        }
    }
}
