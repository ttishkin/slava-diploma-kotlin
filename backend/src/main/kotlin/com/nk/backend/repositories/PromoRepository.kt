package com.nk.backend.repositories

import com.nk.backend.db.PromoCodes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

data class PromoRow(val code: String, val discountPercent: Int, val active: Boolean)

object PromoRepository {

    fun findByCode(code: String): PromoRow? = transaction {
        PromoCodes.selectAll().where {
            PromoCodes.code eq code.uppercase()
        }.firstOrNull()?.let {
            PromoRow(
                code = it[PromoCodes.code],
                discountPercent = it[PromoCodes.discountPercent],
                active = it[PromoCodes.active]
            )
        }
    }

    fun findActiveByCode(code: String): PromoRow? = transaction {
        PromoCodes.selectAll().where {
            (PromoCodes.code eq code.uppercase()) and (PromoCodes.active eq true)
        }.firstOrNull()?.let {
            PromoRow(
                code = it[PromoCodes.code],
                discountPercent = it[PromoCodes.discountPercent],
                active = true
            )
        }
    }
}
