package com.nk.backend.routes

import com.nk.backend.models.CartRequest
import com.nk.backend.plugins.userId
import com.nk.backend.repositories.CartRepository
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cartRoutes() {
    authenticate("auth-required") {
        route("/api/cart") {
            get {
                val items = CartRepository.findByUserId(call.userId())
                call.respond(items)
            }

            put {
                val req = call.receive<CartRequest>()
                CartRepository.replaceAll(
                    call.userId(),
                    req.items.map { it.productId to it.qty }
                )
                val items = CartRepository.findByUserId(call.userId())
                call.respond(items)
            }
        }
    }
}
