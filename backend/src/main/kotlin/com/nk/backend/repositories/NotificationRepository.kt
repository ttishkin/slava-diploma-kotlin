package com.nk.backend.repositories

import com.nk.backend.db.Notifications
import com.nk.backend.db.Orders
import com.nk.backend.models.NotificationDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object NotificationRepository {

    fun create(orderId: Int, email: String?, message: String) = transaction {
        Notifications.insert {
            it[Notifications.orderId] = orderId
            it[Notifications.email] = email
            it[Notifications.message] = message
        }
    }

    fun findByUserEmail(email: String): List<NotificationDto> = transaction {
        Notifications.selectAll().where { Notifications.email eq email }
            .orderBy(Notifications.createdAt to SortOrder.DESC)
            .map { it.toDto() }
    }

    fun findAll(): List<NotificationDto> = transaction {
        Notifications.selectAll()
            .orderBy(Notifications.createdAt to SortOrder.DESC)
            .map { it.toDto() }
    }

    private fun ResultRow.toDto() = NotificationDto(
        id = this[Notifications.id],
        orderId = this[Notifications.orderId],
        email = this[Notifications.email],
        message = this[Notifications.message],
        createdAt = this[Notifications.createdAt].toString()
    )
}
