package com.nk.backend.repositories

import com.nk.backend.db.PickupPoints
import com.nk.backend.models.PickupPointDto
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object PickupPointRepository {

    fun findAll(): List<PickupPointDto> = transaction {
        PickupPoints.selectAll().map {
            PickupPointDto(
                id = it[PickupPoints.id],
                name = it[PickupPoints.name],
                address = it[PickupPoints.address],
                lat = it[PickupPoints.lat],
                lng = it[PickupPoints.lng]
            )
        }
    }
}
