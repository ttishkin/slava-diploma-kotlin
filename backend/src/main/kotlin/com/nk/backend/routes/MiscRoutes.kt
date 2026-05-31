package com.nk.backend.routes

import com.nk.backend.models.PromoCheckResponse
import com.nk.backend.plugins.userId
import com.nk.backend.repositories.NotificationRepository
import com.nk.backend.repositories.PickupPointRepository
import com.nk.backend.repositories.PromoRepository
import com.nk.backend.repositories.UserRepository
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.miscRoutes() {
    route("/api") {
        // Пункты самовывоза
        get("/pickup-points") {
            call.respond(PickupPointRepository.findAll())
        }

        // Проверка промокода
        get("/promo/{code}") {
            val code = call.parameters["code"] ?: ""
            val promo = PromoRepository.findActiveByCode(code)
            if (promo != null) {
                call.respond(PromoCheckResponse(
                    valid = true,
                    code = promo.code,
                    discountPercent = promo.discountPercent
                ))
            } else {
                call.respond(PromoCheckResponse(valid = false))
            }
        }

        // Уведомления пользователя
        authenticate("auth-required") {
            get("/notifications") {
                val user = UserRepository.findById(call.userId())
                val notifications = if (user != null) {
                    NotificationRepository.findByUserEmail(user.email)
                } else emptyList()
                call.respond(notifications)
            }
        }
    }
}
