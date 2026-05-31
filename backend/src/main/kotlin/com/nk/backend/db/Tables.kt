package com.nk.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// === Пользователи ===
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 255).nullable()
    val sex = varchar("sex", 10).nullable()
    val age = integer("age").nullable()
    val height = integer("height").nullable()
    val weight = integer("weight").nullable()
    val activity = double("activity").nullable()
    val goal = varchar("goal", 20).nullable()
    val kcalNorm = integer("kcal_norm").nullable()
    val points = integer("points").default(0)
    val role = varchar("role", 20).default("user")
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// === Категории ===
object Categories : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    val color = varchar("color", 20)
    val glyph = varchar("glyph", 10)

    override val primaryKey = PrimaryKey(id)
}

// === Продукты ===
object Products : Table("products") {
    val id = integer("id").autoIncrement()
    val categoryId = integer("category_id").references(Categories.id)
    val name = varchar("name", 255)
    val kcal = integer("kcal")
    val protein = double("protein")
    val fat = double("fat")
    val carb = double("carb")
    val grams = integer("grams")
    val price = integer("price")
    val sostav = text("sostav").nullable()
    val benefit = text("benefit").nullable()
    val isHit = bool("is_hit").default(false)
    val isNovelty = bool("is_novelty").default(false)
    val imageUrl = varchar("image_url", 500).nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// === Теги продуктов ===
object ProductTags : Table("product_tags") {
    val productId = integer("product_id").references(Products.id)
    val tag = varchar("tag", 50)

    override val primaryKey = PrimaryKey(productId, tag)
}

// === Заказы ===
object Orders : Table("orders") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val no = varchar("no", 20).uniqueIndex()
    val total = integer("total")
    val delivery = integer("delivery")
    val bonusEarned = integer("bonus_earned").default(0)
    val bonusSpent = integer("bonus_spent").default(0)
    val promoCode = varchar("promo_code", 50).nullable()
    val status = varchar("status", 20).default("new")
    val addressId = integer("address_id").nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// === Позиции заказа ===
object OrderItems : Table("order_items") {
    val id = integer("id").autoIncrement()
    val orderId = integer("order_id").references(Orders.id)
    val productId = integer("product_id").references(Products.id)
    val qty = integer("qty")
    val price = integer("price")

    override val primaryKey = PrimaryKey(id)
}

// === Отзывы ===
object Reviews : Table("reviews") {
    val id = integer("id").autoIncrement()
    val productId = integer("product_id").references(Products.id)
    val userId = integer("user_id").references(Users.id).nullable()
    val author = varchar("author", 100).nullable()
    val rating = integer("rating")
    val text = text("text").nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// === Бонусные транзакции ===
object BonusTransactions : Table("bonus_transactions") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val orderId = integer("order_id").references(Orders.id).nullable()
    val amount = integer("amount")
    val type = varchar("type", 10) // "earn" | "spend"
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// === Дневник питания ===
object DiaryEntries : Table("diary_entries") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val productId = integer("product_id").references(Products.id).nullable()
    val name = varchar("name", 255)
    val qty = integer("qty").default(1)
    val grams = integer("grams")
    val kcal = integer("kcal")
    val meal = varchar("meal", 50).default("Перекус")
    val day = varchar("day", 10) // YYYY-MM-DD
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// === Адреса доставки ===
object Addresses : Table("addresses") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val label = varchar("label", 100).nullable()
    val address = varchar("address", 500)
    val lat = double("lat").nullable()
    val lng = double("lng").nullable()
    val isPickup = bool("is_pickup").default(false)
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}

// === Пункты самовывоза ===
object PickupPoints : Table("pickup_points") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val address = varchar("address", 500)
    val lat = double("lat").nullable()
    val lng = double("lng").nullable()

    override val primaryKey = PrimaryKey(id)
}

// === Промокоды ===
object PromoCodes : Table("promo_codes") {
    val id = integer("id").autoIncrement()
    val code = varchar("code", 50).uniqueIndex()
    val discountPercent = integer("discount_percent")
    val active = bool("active").default(true)

    override val primaryKey = PrimaryKey(id)
}

// === Корзина ===
object CartItems : Table("cart_items") {
    val userId = integer("user_id").references(Users.id)
    val productId = integer("product_id").references(Products.id)
    val qty = integer("qty")

    override val primaryKey = PrimaryKey(userId, productId)
}

// === Избранное ===
object Favorites : Table("favorites") {
    val userId = integer("user_id").references(Users.id)
    val productId = integer("product_id").references(Products.id)

    override val primaryKey = PrimaryKey(userId, productId)
}

// === Уведомления ===
object Notifications : Table("notifications") {
    val id = integer("id").autoIncrement()
    val orderId = integer("order_id").references(Orders.id).nullable()
    val email = varchar("email", 255).nullable()
    val message = text("message")
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
