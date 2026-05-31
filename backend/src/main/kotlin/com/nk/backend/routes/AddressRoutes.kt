package com.nk.backend.routes

import com.nk.backend.models.CreateAddressRequest
import com.nk.backend.models.MessageResponse
import com.nk.backend.plugins.badRequest
import com.nk.backend.plugins.userId
import com.nk.backend.repositories.AddressRepository
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.addressRoutes() {
    authenticate("auth-required") {
        route("/api/addresses") {
            get {
                call.respond(AddressRepository.findByUserId(call.userId()))
            }

            post {
                val req = call.receive<CreateAddressRequest>()
                if (req.address.isBlank()) badRequest("Адрес обязателен")
                val address = AddressRepository.create(
                    userId = call.userId(),
                    address = req.address,
                    label = req.label,
                    lat = req.lat,
                    lng = req.lng
                )
                call.respond(address)
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete
                AddressRepository.delete(id, call.userId())
                call.respond(MessageResponse("Удалено"))
            }
        }
    }
}
