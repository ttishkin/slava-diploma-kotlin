package com.nk.backend.routes

import com.nk.backend.models.CreateOrderRequest
import com.nk.backend.plugins.userId
import com.nk.backend.services.OrderService
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes() {
    authenticate("auth-required") {
        route("/api/orders") {
            post {
                val req = call.receive<CreateOrderRequest>()
                val order = OrderService.create(call.userId(), req)
                call.respond(order)
            }

            get {
                val orders = OrderService.getByUser(call.userId())
                call.respond(orders)
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get
                val order = OrderService.getById(id, call.userId())
                call.respond(order)
            }
        }
    }
}
