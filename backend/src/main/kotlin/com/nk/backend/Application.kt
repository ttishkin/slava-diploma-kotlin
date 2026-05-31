package com.nk.backend

import com.nk.backend.db.DatabaseFactory
import com.nk.backend.plugins.*
import com.nk.backend.routes.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Плагины
    configureSerialization()
    configureCors()
    configureSecurity()
    configureStatusPages()

    install(CallLogging) {
        level = Level.INFO
    }

    // База данных
    DatabaseFactory.init(environment)

    // Маршруты
    routing {
        authRoutes()
        productRoutes()
        orderRoutes()
        cartRoutes()
        favoriteRoutes()
        reviewRoutes()
        bonusRoutes()
        diaryRoutes()
        addressRoutes()
        miscRoutes()
        adminRoutes()
    }

    log.info("Невский Кондитер API запущен на порту ${environment.config.property("ktor.deployment.port").getString()}")
}
