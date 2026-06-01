package com.nk.backend.routes

import com.nk.backend.models.CreateReviewRequest
import com.nk.backend.plugins.UserIdPrincipal
import com.nk.backend.plugins.badRequest
import com.nk.backend.repositories.ReviewRepository
import com.nk.backend.repositories.UserRepository
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reviewRoutes() {
    // Опциональная авторизация — отзыв можно оставить без токена
    authenticate("auth-required", optional = true) {
        route("/api/reviews") {
            post {
                val req = call.receive<CreateReviewRequest>()
                if (req.rating !in 1..5) badRequest("Рейтинг должен быть от 1 до 5")

                val principal = call.principal<UserIdPrincipal>()
                val userId = principal?.userId
                val author = if (userId != null) {
                    req.author ?: UserRepository.findById(userId)?.name
                } else {
                    req.author
                }

                val review = ReviewRepository.create(
                    productId = req.productId,
                    userId = userId,
                    author = author,
                    rating = req.rating,
                    text = req.text
                )
                call.respond(review)
            }
        }
    }
}
