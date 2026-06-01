package com.nk.backend.repositories

import com.nk.backend.db.*
import com.nk.backend.models.DailySaleDto
import com.nk.backend.models.OrderDto
import com.nk.backend.models.OrderItemDto
import com.nk.backend.models.StatusCountDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

object OrderRepository {

    fun create(
        userId: Int,
        total: Int,
        delivery: Int,
        bonusEarned: Int,
        bonusSpent: Int,
        promoCode: String?,
        addressId: Int?,
        items: List<Pair<Int, Pair<Int, Int>>> // productId -> (qty, price)
    ): OrderDto = transaction {
        val orderNo = "NK-${UUID.randomUUID().toString().take(8).uppercase()}"

        val orderId = Orders.insert {
            it[Orders.userId] = userId
            it[no] = orderNo
            it[Orders.total] = total
            it[Orders.delivery] = delivery
            it[Orders.bonusEarned] = bonusEarned
            it[Orders.bonusSpent] = bonusSpent
            it[Orders.promoCode] = promoCode
            it[status] = "new"
            it[Orders.addressId] = addressId
        } get Orders.id

        val orderItems = items.map { (productId, qtyPrice) ->
            val itemId = OrderItems.insert {
                it[OrderItems.orderId] = orderId
                it[OrderItems.productId] = productId
                it[qty] = qtyPrice.first
                it[price] = qtyPrice.second
            } get OrderItems.id

            OrderItemDto(
                id = itemId,
                productId = productId,
                qty = qtyPrice.first,
                price = qtyPrice.second
            )
        }

        OrderDto(
            id = orderId,
            userId = userId,
            no = orderNo,
            total = total,
            delivery = delivery,
            bonusEarned = bonusEarned,
            bonusSpent = bonusSpent,
            promoCode = promoCode,
            status = "new",
            addressId = addressId,
            createdAt = LocalDateTime.now().toString(),
            items = orderItems
        )
    }

    fun findByUserId(userId: Int): List<OrderDto> = transaction {
        val orders = Orders.selectAll().where { Orders.userId eq userId }
            .orderBy(Orders.createdAt to SortOrder.DESC)
            .toList()

        orders.map { it.toOrderDto() }
    }

    fun findById(id: Int): OrderDto? = transaction {
        Orders.selectAll().where { Orders.id eq id }
            .firstOrNull()?.toOrderDto()
    }

    fun findAll(): List<OrderDto> = transaction {
        val orders = Orders
            .join(Users, JoinType.LEFT, Orders.userId, Users.id)
            .selectAll()
            .orderBy(Orders.createdAt to SortOrder.DESC)
            .toList()

        orders.map { row ->
            row.toOrderDto().copy(userEmail = row.getOrNull(Users.email))
        }
    }

    fun updateStatus(orderId: Int, status: String) = transaction {
        Orders.update({ Orders.id eq orderId }) {
            it[Orders.status] = status
        }
    }

    fun delete(id: Int) = transaction {
        OrderItems.deleteWhere { OrderItems.orderId eq id }
        BonusTransactions.deleteWhere { BonusTransactions.orderId eq id }
        Notifications.deleteWhere { Notifications.orderId eq id }
        Orders.deleteWhere { Orders.id eq id }
    }

    fun countByUserId(userId: Int): Int = transaction {
        Orders.selectAll().where { Orders.userId eq userId }.count().toInt()
    }

    fun totalCount(): Int = transaction {
        Orders.selectAll().count().toInt()
    }

    fun totalRevenue(): Int = transaction {
        Orders.selectAll().sumOf { it[Orders.total] }
    }

    fun countByStatus(): List<StatusCountDto> = transaction {
        Orders.select(Orders.status, Orders.id.count())
            .groupBy(Orders.status)
            .map { StatusCountDto(it[Orders.status], it[Orders.id.count()].toInt()) }
    }

    fun dailySales(days: Int): List<DailySaleDto> = transaction {
        val since = LocalDate.now().minusDays(days.toLong())
        val sinceStr = since.toString()

        val conn = this.connection.connection as java.sql.Connection
        val stmt = conn.prepareStatement(
            "SELECT date(created_at) as day, COUNT(*) as orders, COALESCE(SUM(total),0) as revenue FROM orders WHERE date(created_at) >= ? GROUP BY date(created_at) ORDER BY day"
        )
        stmt.setString(1, sinceStr)
        val rs = stmt.executeQuery()

        val list = mutableListOf<DailySaleDto>()
        while (rs.next()) {
            list.add(DailySaleDto(
                day = rs.getString("day"),
                orders = rs.getInt("orders"),
                revenue = rs.getInt("revenue")
            ))
        }
        rs.close()
        stmt.close()
        list
    }

    private fun ResultRow.toOrderDto(): OrderDto {
        val orderId = this[Orders.id]
        val items = OrderItems
            .join(Products, JoinType.LEFT, OrderItems.productId, Products.id)
            .selectAll().where { OrderItems.orderId eq orderId }
            .map { item ->
                OrderItemDto(
                    id = item[OrderItems.id],
                    productId = item[OrderItems.productId],
                    productName = item.getOrNull(Products.name),
                    qty = item[OrderItems.qty],
                    price = item[OrderItems.price]
                )
            }

        return OrderDto(
            id = orderId,
            userId = this[Orders.userId],
            no = this[Orders.no],
            total = this[Orders.total],
            delivery = this[Orders.delivery],
            bonusEarned = this[Orders.bonusEarned],
            bonusSpent = this[Orders.bonusSpent],
            promoCode = this[Orders.promoCode],
            status = this[Orders.status],
            addressId = this[Orders.addressId],
            createdAt = this[Orders.createdAt].toString(),
            items = items
        )
    }
}
