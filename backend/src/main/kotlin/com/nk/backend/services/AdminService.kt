package com.nk.backend.services

import com.nk.backend.models.*
import com.nk.backend.repositories.*

object AdminService {

    fun getStats(): AdminStatsDto {
        return AdminStatsDto(
            orders = OrderRepository.totalCount(),
            revenue = OrderRepository.totalRevenue(),
            users = UserRepository.findAll().size,
            products = ProductRepository.count(),
            byStatus = OrderRepository.countByStatus()
        )
    }

    fun getAllOrders(): List<OrderDto> {
        return OrderRepository.findAll()
    }

    fun getAllUsers(): List<AdminUserDto> {
        val users = UserRepository.findAll()
        return users.map { user ->
            user.copy(orderCount = OrderRepository.countByUserId(user.id))
        }
    }

    fun getUserById(userId: Int): Map<String, Any?> {
        val user = UserRepository.findById(userId) ?: return emptyMap()
        val orders = OrderRepository.findByUserId(userId)
        val bonuses = BonusRepository.findByUserId(userId)
        return mapOf(
            "user" to UserRepository.toDto(user),
            "orders" to orders,
            "bonuses" to bonuses
        )
    }

    fun getDailySales(days: Int): List<DailySaleDto> {
        return OrderRepository.dailySales(days)
    }

    fun getAllNotifications(): List<NotificationDto> {
        return NotificationRepository.findAll()
    }
}
