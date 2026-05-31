package com.nk.backend.plugins

import com.nk.backend.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

// Кастомное исключение с HTTP-статусом
class ApiException(val statusCode: HttpStatusCode, message: String) : RuntimeException(message)

fun badRequest(message: String): Nothing = throw ApiException(HttpStatusCode.BadRequest, message)
fun notFound(message: String): Nothing = throw ApiException(HttpStatusCode.NotFound, message)
fun forbidden(message: String): Nothing = throw ApiException(HttpStatusCode.Forbidden, message)
fun unauthorized(message: String): Nothing = throw ApiException(HttpStatusCode.Unauthorized, message)

fun Application.configureStatusPages() {
    val log = LoggerFactory.getLogger("ErrorHandler")

    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.statusCode, ErrorResponse(cause.message ?: "Ошибка"))
        }
        exception<Throwable> { call, cause ->
            log.error("Неожиданная ошибка: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Внутренняя ошибка сервера")
            )
        }
    }
}
