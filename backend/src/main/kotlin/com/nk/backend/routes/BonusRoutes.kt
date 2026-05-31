package com.nk.backend.routes

import com.nk.backend.models.BonusResponse
import com.nk.backend.plugins.userId
import com.nk.backend.repositories.BonusRepository
import com.nk.backend.repositories.UserRepository
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bonusRoutes() {
    authenticate("auth-required") {
        get("/api/bonuses") {
            val uid = call.userId()
            val user = UserRepository.findById(uid)
            val transactions = BonusRepository.findByUserId(uid)
            call.respond(BonusResponse(
                balance = user?.points ?: 0,
                transactions = transactions
            ))
        }
    }
}
