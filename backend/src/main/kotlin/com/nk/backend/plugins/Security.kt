package com.nk.backend.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

// Утилиты для JWT
object JwtConfig {
    lateinit var secret: String
    lateinit var issuer: String
    lateinit var audience: String
    var expiresInDays: Long = 7

    fun init(environment: ApplicationEnvironment) {
        secret = environment.config.property("app.jwt.secret").getString()
        issuer = environment.config.property("app.jwt.issuer").getString()
        audience = environment.config.property("app.jwt.audience").getString()
        expiresInDays = environment.config.property("app.jwt.expiresInDays").getString().toLong()
    }

    fun generateToken(userId: Int, role: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + expiresInDays * 24 * 3600 * 1000))
            .sign(Algorithm.HMAC256(secret))
    }
}

fun Application.configureSecurity() {
    JwtConfig.init(environment)

    install(Authentication) {
        // Обязательная авторизация
        jwt("auth-required") {
            verifier(
                JWT.require(Algorithm.HMAC256(JwtConfig.secret))
                    .withIssuer(JwtConfig.issuer)
                    .withAudience(JwtConfig.audience)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                val role = credential.payload.getClaim("role").asString()
                if (userId != null) {
                    UserIdPrincipal(userId, role ?: "user")
                } else null
            }
        }

        // Опциональная авторизация (не падает если нет токена)
        jwt("auth-optional") {
            verifier(
                JWT.require(Algorithm.HMAC256(JwtConfig.secret))
                    .withIssuer(JwtConfig.issuer)
                    .withAudience(JwtConfig.audience)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asInt()
                val role = credential.payload.getClaim("role").asString()
                if (userId != null) UserIdPrincipal(userId, role ?: "user") else null
            }
            skipWhen { true } // Не возвращаем 401 если токена нет
        }
    }
}

// Principal с userId и role
data class UserIdPrincipal(val userId: Int, val role: String) : Principal

// Расширение для быстрого доступа к userId из маршрутов
fun io.ktor.server.routing.RoutingCall.userId(): Int {
    return principal<UserIdPrincipal>()?.userId
        ?: throw IllegalStateException("Пользователь не авторизован")
}

fun io.ktor.server.routing.RoutingCall.userRole(): String {
    return principal<UserIdPrincipal>()?.role ?: "user"
}

fun io.ktor.server.routing.RoutingCall.userIdOrNull(): Int? {
    return principal<UserIdPrincipal>()?.userId
}
