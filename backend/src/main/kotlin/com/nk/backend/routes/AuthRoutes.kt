package com.nk.backend.routes

import com.nk.backend.models.LoginRequest
import com.nk.backend.models.RegisterRequest
import com.nk.backend.plugins.userId
import com.nk.backend.services.AuthService
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    route("/api/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            val result = AuthService.register(req)
            call.respond(result)
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val result = AuthService.login(req)
            call.respond(result)
        }

        authenticate("auth-required") {
            get("/me") {
                val user = AuthService.getProfile(call.userId())
                call.respond(user)
            }
        }
    }
}
