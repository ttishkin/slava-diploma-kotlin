package com.nk.backend.repositories

import com.nk.backend.db.Categories
import com.nk.backend.models.CategoryDto
import org.jetbrains.exposed.sql.selectAll
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
}
