package com.nk.backend.repositories

import com.nk.backend.db.DiaryEntries
import com.nk.backend.models.DiaryEntryDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object DiaryRepository {

    fun findByUserAndDay(userId: Int, day: String): List<DiaryEntryDto> = transaction {
        DiaryEntries.selectAll().where {
            (DiaryEntries.userId eq userId) and (DiaryEntries.day eq day)
        }.orderBy(DiaryEntries.createdAt to SortOrder.ASC).map { it.toDto() }
    }

    fun create(
        userId: Int, productId: Int?, name: String,
        qty: Int, grams: Int, kcal: Int, meal: String, day: String
    ): DiaryEntryDto = transaction {
        val id = DiaryEntries.insert {
            it[DiaryEntries.userId] = userId
            it[DiaryEntries.productId] = productId
            it[DiaryEntries.name] = name
            it[DiaryEntries.qty] = qty
            it[DiaryEntries.grams] = grams
            it[DiaryEntries.kcal] = kcal
            it[DiaryEntries.meal] = meal
            it[DiaryEntries.day] = day
        } get DiaryEntries.id

        DiaryEntries.selectAll().where { DiaryEntries.id eq id }.first().toDto()
    }

    fun updateQty(id: Int, userId: Int, qty: Int) = transaction {
        DiaryEntries.update({ (DiaryEntries.id eq id) and (DiaryEntries.userId eq userId) }) {
            it[DiaryEntries.qty] = qty
        }
    }

    fun delete(id: Int, userId: Int) = transaction {
        DiaryEntries.deleteWhere { (DiaryEntries.id eq id) and (DiaryEntries.userId eq userId) }
    }

    fun findById(id: Int): DiaryEntryDto? = transaction {
        DiaryEntries.selectAll().where { DiaryEntries.id eq id }.firstOrNull()?.toDto()
    }

    private fun ResultRow.toDto() = DiaryEntryDto(
        id = this[DiaryEntries.id],
        productId = this[DiaryEntries.productId],
        name = this[DiaryEntries.name],
        qty = this[DiaryEntries.qty],
        grams = this[DiaryEntries.grams],
        kcal = this[DiaryEntries.kcal],
        meal = this[DiaryEntries.meal],
        day = this[DiaryEntries.day],
        createdAt = this[DiaryEntries.createdAt].toString()
    )
}
