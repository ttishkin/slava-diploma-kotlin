package com.nk.backend.repositories

import com.nk.backend.db.*
import com.nk.backend.models.AdminUserDto
import com.nk.backend.models.UserDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {

    fun findByEmail(email: String): UserRow? = transaction {
        Users.selectAll().where { Users.email eq email }.firstOrNull()?.toUserRow()
    }

    fun findById(id: Int): UserRow? = transaction {
        Users.selectAll().where { Users.id eq id }.firstOrNull()?.toUserRow()
    }

    fun create(
        email: String, passwordHash: String, name: String?,
        sex: String?, age: Int?, height: Int?, weight: Int?,
        activity: Double?, goal: String?, kcalNorm: Int?
    ): Int = transaction {
        Users.insert {
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.name] = name
            it[Users.sex] = sex
            it[Users.age] = age
            it[Users.height] = height
            it[Users.weight] = weight
            it[Users.activity] = activity
            it[Users.goal] = goal
            it[Users.kcalNorm] = kcalNorm
            it[Users.points] = 200 // Приветственный бонус
        } get Users.id
    }

    fun updatePoints(userId: Int, delta: Int) = transaction {
        val current = Users.selectAll().where { Users.id eq userId }
            .first()[Users.points]
        val newPoints = maxOf(0, current + delta)
        Users.update({ Users.id eq userId }) {
            it[points] = newPoints
        }
    }

    fun addPoints(userId: Int, amount: Int) = transaction {
        Users.update({ Users.id eq userId }) {
            with(SqlExpressionBuilder) {
                it[points] = points + amount
            }
        }
    }

    fun subtractPoints(userId: Int, amount: Int) = transaction {
        // Сначала проверяем баланс
        val current = Users.selectAll().where { Users.id eq userId }
            .first()[Users.points]
        val newPoints = maxOf(0, current - amount)
        Users.update({ Users.id eq userId }) {
            it[points] = newPoints
        }
    }

    fun delete(id: Int) = transaction {
        // Удаляем связанные данные пользователя
        DiaryEntries.deleteWhere { DiaryEntries.userId eq id }
        CartItems.deleteWhere { CartItems.userId eq id }
        Favorites.deleteWhere { Favorites.userId eq id }
        BonusTransactions.deleteWhere { BonusTransactions.userId eq id }
        Addresses.deleteWhere { Addresses.userId eq id }
        Reviews.deleteWhere { Reviews.userId eq id }
        // Удаляем позиции заказов и заказы пользователя
        val orderIds = Orders.selectAll().where { Orders.userId eq id }.map { it[Orders.id] }
        if (orderIds.isNotEmpty()) {
            for (oid in orderIds) {
                OrderItems.deleteWhere { OrderItems.orderId eq oid }
                Notifications.deleteWhere { Notifications.orderId eq oid }
            }
            Orders.deleteWhere { Orders.userId eq id }
        }
        Users.deleteWhere { Users.id eq id }
    }

    fun updateRole(id: Int, role: String) = transaction {
        Users.update({ Users.id eq id }) {
            it[Users.role] = role
        }
    }

    fun toDto(row: UserRow) = UserDto(
        id = row.id, email = row.email, name = row.name,
        sex = row.sex, age = row.age, height = row.height,
        weight = row.weight, activity = row.activity, goal = row.goal,
        kcalNorm = row.kcalNorm, points = row.points, role = row.role
    )

    fun findAll(): List<AdminUserDto> = transaction {
        Users.selectAll().map { row ->
            AdminUserDto(
                id = row[Users.id],
                email = row[Users.email],
                name = row[Users.name],
                role = row[Users.role],
                points = row[Users.points],
                createdAt = row[Users.createdAt].toString(),
                orderCount = 0 // Заполняется в сервисе
            )
        }
    }

    private fun ResultRow.toUserRow() = UserRow(
        id = this[Users.id],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        name = this[Users.name],
        sex = this[Users.sex],
        age = this[Users.age],
        height = this[Users.height],
        weight = this[Users.weight],
        activity = this[Users.activity],
        goal = this[Users.goal],
        kcalNorm = this[Users.kcalNorm],
        points = this[Users.points],
        role = this[Users.role]
    )
}

data class UserRow(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val name: String?,
    val sex: String?,
    val age: Int?,
    val height: Int?,
    val weight: Int?,
    val activity: Double?,
    val goal: String?,
    val kcalNorm: Int?,
    val points: Int,
    val role: String
)
