package com.nk.backend.repositories

import com.nk.backend.db.Addresses
import com.nk.backend.models.AddressDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object AddressRepository {

    fun findByUserId(userId: Int): List<AddressDto> = transaction {
        Addresses.selectAll().where { Addresses.userId eq userId }.map { it.toDto() }
    }

    fun create(userId: Int, address: String, label: String?, lat: Double?, lng: Double?): AddressDto = transaction {
        val id = Addresses.insert {
            it[Addresses.userId] = userId
            it[Addresses.address] = address
            it[Addresses.label] = label
            it[Addresses.lat] = lat
            it[Addresses.lng] = lng
        } get Addresses.id

        Addresses.selectAll().where { Addresses.id eq id }.first().toDto()
    }

    fun delete(id: Int, userId: Int) = transaction {
        Addresses.deleteWhere { (Addresses.id eq id) and (Addresses.userId eq userId) }
    }

    private fun ResultRow.toDto() = AddressDto(
        id = this[Addresses.id],
        label = this[Addresses.label],
        address = this[Addresses.address],
        lat = this[Addresses.lat],
        lng = this[Addresses.lng],
        isPickup = this[Addresses.isPickup]
    )
}
