package com.nk.backend.routes

import com.nk.backend.plugins.notFound
import com.nk.backend.repositories.CategoryRepository
import com.nk.backend.repositories.ProductRepository
import com.nk.backend.repositories.ReviewRepository
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes() {
    route("/api") {
        get("/products") {
            val query = call.request.queryParameters["q"]
            val category = call.request.queryParameters["category"]
            val tag = call.request.queryParameters["tag"]
            val sort = call.request.queryParameters["sort"]

            val products = ProductRepository.findAll(query, category, tag, sort)
            call.respond(products)
        }

        get("/products/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: notFound("Некорректный id")
            val product = ProductRepository.findById(id) ?: notFound("Продукт не найден")

            // Добавляем отзывы
            val reviews = ReviewRepository.findByProductId(id)
            call.respond(product.copy(reviews = reviews))
        }

        get("/products/{id}/reviews") {
            val id = call.parameters["id"]?.toIntOrNull() ?: notFound("Некорректный id")
            val reviews = ReviewRepository.findByProductId(id)
            call.respond(reviews)
        }

        get("/categories") {
            call.respond(CategoryRepository.findAll())
        }
    }
}
