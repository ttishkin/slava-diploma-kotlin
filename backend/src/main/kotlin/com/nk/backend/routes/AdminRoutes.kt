package com.nk.backend.routes

import com.nk.backend.models.UpdateStatusRequest
import com.nk.backend.plugins.forbidden
import com.nk.backend.plugins.userRole
import com.nk.backend.services.AdminService
import com.nk.backend.services.OrderService
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes() {
    authenticate("auth-required") {
        route("/api/admin") {
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

            get("/stats") {
                requireAdmin(call)
                call.respond(AdminService.getStats())
            }

            get("/users") {
                requireAdmin(call)
                call.respond(AdminService.getAllUsers())
            }

            get("/users/{id}") {
                requireAdmin(call)
                val id = call.parameters["id"]?.toIntOrNull() ?: return@get
                call.respond(AdminService.getUserById(id))
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
        }
    }
}

private fun requireAdmin(call: io.ktor.server.routing.RoutingCall) {
    if (call.userRole() != "admin") forbidden("Доступ только для администраторов")
}
