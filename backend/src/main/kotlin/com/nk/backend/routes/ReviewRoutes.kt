package com.nk.backend.routes

import com.nk.backend.models.CreateReviewRequest
import com.nk.backend.plugins.UserIdPrincipal
import com.nk.backend.plugins.badRequest
import com.nk.backend.repositories.ReviewRepository
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reviewRoutes() {
    route("/api/reviews") {
        post {
            val req = call.receive<CreateReviewRequest>()
            if (req.rating !in 1..5) badRequest("Рейтинг должен быть от 1 до 5")

            // userId опционален — отзыв можно оставить без авторизации
            val userId: Int? = try {
                call.principal<UserIdPrincipal>()?.userId
            } catch (_: Exception) { null }

            val review = ReviewRepository.create(
                productId = req.productId,
                userId = userId,
                author = req.author,
                rating = req.rating,
                text = req.text
            )
            call.respond(review)
        }
    }
}
