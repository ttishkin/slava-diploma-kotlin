package com.nk.backend.routes

import com.nk.backend.models.*
import com.nk.backend.plugins.forbidden
import com.nk.backend.plugins.userRole
import com.nk.backend.repositories.CategoryRepository
import com.nk.backend.repositories.OrderRepository
import com.nk.backend.repositories.ProductRepository
import com.nk.backend.repositories.UserRepository
import com.nk.backend.services.AdminService
import com.nk.backend.services.OrderService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes() {
    authenticate("auth-required") {
        route("/api/admin") {
            // === Заказы ===

            get("/orders") {
                requireAdmin(call)
                call.respond(AdminService.getAllOrders())
            }

            patch("/orders/{id}/status") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@patch
                val req = call.receive<UpdateStatusRequest>()
                val order = OrderService.updateStatus(id, req.status)
                call.respond(order)
            }

            delete("/orders/{id}") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete
                OrderRepository.delete(id)
                call.respond(MessageResponse("ok"))
            }

            // === Статистика ===

            get("/stats") {
                requireAdmin(call)
                call.respond(AdminService.getStats())
            }

            get("/sales") {
                requireAdmin(call)
                val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 14
                call.respond(AdminService.getDailySales(days))
            }

            get("/notifications") {
                requireAdmin(call)
                call.respond(AdminService.getAllNotifications())
            }

            // === Пользователи ===

            get("/users") {
                requireAdmin(call)
                call.respond(AdminService.getAllUsers())
            }

            get("/users/{id}") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get
                call.respond(AdminService.getUserById(id))
            }

            patch("/users/{id}/role") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@patch
                val req = call.receive<UpdateUserRoleRequest>()
                UserRepository.updateRole(id, req.role)
                val user = UserRepository.findById(id) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Пользователь не найден"))
                    return@patch
                }
                call.respond(UserRepository.toDto(user))
            }

            delete("/users/{id}") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete
                UserRepository.delete(id)
                call.respond(MessageResponse("ok"))
            }

            // === Продукты ===

            post("/products") {
                requireAdmin(call)
                val req = call.receive<CreateProductRequest>()
                val productId = ProductRepository.create(
                    name = req.name, categoryId = req.categoryId,
                    kcal = req.kcal, protein = req.protein, fat = req.fat, carb = req.carb,
                    grams = req.grams, price = req.price,
                    sostav = req.sostav, benefit = req.benefit,
                    isHit = req.isHit, isNovelty = req.isNovelty,
                    imageUrl = req.imageUrl, tags = req.tags
                )
                val product = ProductRepository.findById(productId)!!
                call.respond(HttpStatusCode.Created, product)
            }

            patch("/products/{id}") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@patch
                if (!ProductRepository.existsById(id)) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Продукт не найден"))
                    return@patch
                }
                val req = call.receive<UpdateProductRequest>()
                ProductRepository.update(
                    id = id, name = req.name, categoryId = req.categoryId,
                    kcal = req.kcal, protein = req.protein, fat = req.fat, carb = req.carb,
                    grams = req.grams, price = req.price,
                    sostav = req.sostav, benefit = req.benefit,
                    isHit = req.isHit, isNovelty = req.isNovelty,
                    imageUrl = req.imageUrl, tags = req.tags
                )
                val product = ProductRepository.findById(id)!!
                call.respond(product)
            }

            delete("/products/{id}") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete
                ProductRepository.delete(id)
                call.respond(MessageResponse("ok"))
            }

            // === Категории ===

            post("/categories") {
                requireAdmin(call)
                val req = call.receive<CreateCategoryRequest>()
                val categoryId = CategoryRepository.create(
                    name = req.name, color = req.color, glyph = req.glyph
                )
                val category = CategoryRepository.findById(categoryId)!!
                call.respond(HttpStatusCode.Created, category)
            }

            patch("/categories/{id}") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@patch
                val existing = CategoryRepository.findById(id) ?: run {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Категория не найдена"))
                    return@patch
                }
                val req = call.receive<UpdateCategoryRequest>()
                CategoryRepository.update(
                    id = id, name = req.name, color = req.color, glyph = req.glyph
                )
                val category = CategoryRepository.findById(id)!!
                call.respond(category)
            }

            delete("/categories/{id}") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete
                CategoryRepository.delete(id)
                call.respond(MessageResponse("ok"))
            }
        }
    }
}

private fun requireAdmin(call: io.ktor.server.routing.RoutingCall) {
    if (call.userRole() != "admin") forbidden("Доступ только для администраторов")
}
