package com.nk.backend.repositories

import com.nk.backend.db.Categories
import com.nk.backend.db.Products
import com.nk.backend.models.CategoryDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CategoryRepository {

    fun findAll(): List<CategoryDto> = transaction {
        Categories.selectAll().map {
            CategoryDto(
                id = it[Categories.id],
                name = it[Categories.name],
                color = it[Categories.color],
                glyph = it[Categories.glyph]
            )
        }
    }

    fun findById(id: Int): CategoryDto? = transaction {
        Categories.selectAll().where { Categories.id eq id }.firstOrNull()?.let {
            CategoryDto(
                id = it[Categories.id],
                name = it[Categories.name],
                color = it[Categories.color],
                glyph = it[Categories.glyph]
            )
        }
    }

    fun create(name: String, color: String, glyph: String): Int = transaction {
        Categories.insert {
            it[Categories.name] = name
            it[Categories.color] = color
            it[Categories.glyph] = glyph
        } get Categories.id
    }

    fun update(id: Int, name: String?, color: String?, glyph: String?) = transaction {
        Categories.update({ Categories.id eq id }) {
            if (name != null) it[Categories.name] = name
            if (color != null) it[Categories.color] = color
            if (glyph != null) it[Categories.glyph] = glyph
        }
    }

    fun delete(id: Int) = transaction {
        // Обнуляем categoryId у продуктов этой категории
        Products.update({ Products.categoryId eq id }) {
            it[categoryId] = 0
        }
        Categories.deleteWhere { Categories.id eq id }
    }
}
