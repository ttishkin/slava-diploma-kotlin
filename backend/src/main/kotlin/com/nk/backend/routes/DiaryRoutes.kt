package com.nk.backend.routes

import com.nk.backend.models.CreateDiaryEntryRequest
import com.nk.backend.models.MessageResponse
import com.nk.backend.models.UpdateDiaryEntryRequest
import com.nk.backend.plugins.badRequest
import com.nk.backend.plugins.userId
import com.nk.backend.repositories.DiaryRepository
import com.nk.backend.repositories.ProductRepository
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate

fun Route.diaryRoutes() {
    authenticate("auth-required") {
        route("/api/diary") {
            get {
                val day = call.request.queryParameters["day"] ?: LocalDate.now().toString()
                val entries = DiaryRepository.findByUserAndDay(call.userId(), day)
                call.respond(entries)
            }

            post {
                val req = call.receive<CreateDiaryEntryRequest>()
                val day = req.day ?: LocalDate.now().toString()

                // Определяем название, граммы и калории
                val name: String
                val grams: Int
                val kcal: Int

                if (req.productId != null) {
                    val product = ProductRepository.findById(req.productId)
                        ?: badRequest("Продукт не найден")
                    name = product.name
                    grams = product.grams * req.qty
                    kcal = product.kcal * req.qty
                } else {
                    name = req.name ?: badRequest("Укажите productId или name")
                    grams = req.grams ?: 100
                    kcal = req.kcal ?: 0
                }

                val entry = DiaryRepository.create(
                    userId = call.userId(),
                    productId = req.productId,
                    name = name,
                    qty = req.qty,
                    grams = grams,
                    kcal = kcal,
                    meal = req.meal,
                    day = day
                )
                call.respond(entry)
            }

            patch("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@patch
                val req = call.receive<UpdateDiaryEntryRequest>()
                DiaryRepository.updateQty(id, call.userId(), req.qty)
                val entry = DiaryRepository.findById(id)
                if (entry != null) call.respond(entry)
                else call.respond(MessageResponse("Обновлено"))
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete
                DiaryRepository.delete(id, call.userId())
                call.respond(MessageResponse("Удалено"))
            }
        }
    }
}
