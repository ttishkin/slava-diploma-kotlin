package com.nk.backend.routes

import com.nk.backend.models.FavoritesRequest
import com.nk.backend.plugins.userId
import com.nk.backend.repositories.FavoriteRepository
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.favoriteRoutes() {
    authenticate("auth-required") {
        route("/api/favorites") {
            get {
                val ids = FavoriteRepository.findByUserId(call.userId())
                call.respond(mapOf("productIds" to ids))
            }

            put {
                val req = call.receive<FavoritesRequest>()
                FavoriteRepository.replaceAll(call.userId(), req.productIds)
                call.respond(mapOf("productIds" to req.productIds))
            }
        }
    }
}
