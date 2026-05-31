package com.nk.backend.services

import com.nk.backend.models.*
import com.nk.backend.plugins.badRequest
import com.nk.backend.plugins.forbidden
import com.nk.backend.plugins.notFound
import com.nk.backend.repositories.*

object OrderService {

    fun create(userId: Int, req: CreateOrderRequest): OrderDto {
        if (req.items.isEmpty()) badRequest("Корзина пуста")

        // Считаем subtotal
        var subtotal = 0
        val resolvedItems = req.items.map { item ->
            if (item.qty < 1) badRequest("Количество должно быть >= 1")
            val price = ProductRepository.getPriceById(item.id)
                ?: badRequest("Продукт с id=${item.id} не найден")
            subtotal += price * item.qty
            item.id to (item.qty to price)
        }

        // Доставка
        val delivery = if (subtotal >= 1000) 0 else 199

        // Промокод
        var promoDiscount = 0
        var promoCodeUsed: String? = null
        if (!req.promoCode.isNullOrBlank()) {
            val promo = PromoRepository.findActiveByCode(req.promoCode)
            if (promo != null && promo.discountPercent > 0) {
                promoDiscount = subtotal * promo.discountPercent / 100
                promoCodeUsed = promo.code
            }
        }

        // Бонусы
        val user = UserRepository.findById(userId) ?: badRequest("Пользователь не найден")
        val maxBonusSpend = subtotal * 30 / 100 // Максимум 30%
        val bonusSpent = minOf(req.bonusSpend, maxBonusSpend, user.points)

        // Итого (не может быть отрицательным)
        val total = maxOf(0, subtotal + delivery - promoDiscount - bonusSpent)

        // Бонусы за заказ — 5% от subtotal
        val bonusEarned = subtotal * 5 / 100

        // Создаём заказ в транзакции
        val order = OrderRepository.create(
            userId = userId,
            total = total,
            delivery = delivery,
            bonusEarned = bonusEarned,
            bonusSpent = bonusSpent,
            promoCode = promoCodeUsed,
            addressId = req.addressId,
            items = resolvedItems
        )

        // Обновляем бонусы пользователя
        if (bonusSpent > 0) {
            UserRepository.subtractPoints(userId, bonusSpent)
            BonusRepository.create(userId, order.id, bonusSpent, "spend")
        }
        UserRepository.addPoints(userId, bonusEarned)
        BonusRepository.create(userId, order.id, bonusEarned, "earn")

        // Очищаем корзину
        CartRepository.clear(userId)

        return order
    }

    fun getByUser(userId: Int): List<OrderDto> {
        return OrderRepository.findByUserId(userId)
    }

    fun getById(orderId: Int, userId: Int): OrderDto {
        val order = OrderRepository.findById(orderId) ?: notFound("Заказ не найден")
        return order
    }

    fun updateStatus(orderId: Int, status: String): OrderDto {
        val validStatuses = listOf("new", "processing", "shipped", "delivered", "cancelled")
        if (status !in validStatuses) badRequest("Недопустимый статус: $status")

        OrderRepository.updateStatus(orderId, status)
        val order = OrderRepository.findById(orderId) ?: notFound("Заказ не найден")

        // Уведомление
        val user = UserRepository.findById(order.id)
        NotificationRepository.create(
            orderId = orderId,
            email = user?.email,
            message = "Статус заказа ${order.no} изменён на «$status»"
        )

        return order
    }
}
