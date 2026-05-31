package com.nk.backend.db

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import java.io.File

object DatabaseFactory {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun init(environment: ApplicationEnvironment) {
        val dbPath = environment.config.property("app.db.path").getString()

        // Создаём папку если нет
        File(dbPath).parentFile?.mkdirs()

        // foreign_keys и journal_mode через параметры URL (до транзакции)
        Database.connect(
            "jdbc:sqlite:$dbPath?journal_mode=WAL&foreign_keys=ON",
            driver = "org.sqlite.JDBC"
        )

        transaction {

            // Создаём таблицы
            SchemaUtils.create(
                Users, Categories, Products, ProductTags,
                Orders, OrderItems, Reviews, BonusTransactions,
                DiaryEntries, Addresses, PickupPoints, PromoCodes,
                CartItems, Favorites, Notifications
            )

            // Сидирование если БД пустая
            if (Categories.selectAll().empty()) {
                log.info("БД пустая — заполняю демо-данными...")
                seed()
            }
        }
    }

    private fun seed() {
        // Категории
        val categoryIds = mutableMapOf<String, Int>()
        listOf(
            Triple("Злаковые батончики", "#F2A65A", "🌾"),
            Triple("Фруктовый грильяж", "#FF6B6B", "🍓"),
            Triple("Ореховые батончики", "#B5651D", "🥜"),
            Triple("Протеиновые батончики", "#5E5CE6", "💪"),
            Triple("Мармелад", "#FF375F", "🍬"),
            Triple("Желейные конфеты", "#30B0C7", "🟢")
        ).forEach { (name, color, glyph) ->
            val id = Categories.insert {
                it[Categories.name] = name
                it[Categories.color] = color
                it[Categories.glyph] = glyph
            } get Categories.id
            categoryIds[name] = id
        }

        // Продукты
        data class ProductSeed(
            val name: String, val cat: String,
            val kcal: Int, val p: Double, val f: Double, val c: Double,
            val g: Int, val price: Int,
            val tags: List<String> = emptyList(),
            val hit: Boolean = false, val nov: Boolean = false,
            val sostav: String = "", val benefit: String = ""
        )

        val products = listOf(
            // Злаковые батончики
            ProductSeed("So crispy с клубникой", "Злаковые батончики", 410, 3.0, 11.0, 74.0, 40, 65, hit = true,
                sostav = "Злаки, клубника сублимированная, сахар, растительный жир", benefit = "Источник быстрой энергии"),
            ProductSeed("So crispy с шоколадом", "Злаковые батончики", 410, 3.0, 11.0, 74.0, 40, 65, hit = true,
                sostav = "Злаки, какао-порошок, сахар, растительный жир", benefit = "Источник быстрой энергии"),
            ProductSeed("So crispy с кокосом", "Злаковые батончики", 408, 3.0, 12.0, 72.0, 40, 65,
                sostav = "Злаки, кокосовая стружка, сахар, растительный жир", benefit = "Источник клетчатки"),
            ProductSeed("So crispy с клюквой", "Злаковые батончики", 405, 3.0, 10.0, 74.0, 40, 65,
                sostav = "Злаки, клюква сушёная, сахар, растительный жир", benefit = "Витамин C"),
            ProductSeed("So crispy без сахара, с малиной", "Злаковые батончики", 388, 4.0, 11.0, 64.0, 40, 79, listOf("sugar"), nov = true,
                sostav = "Злаки, малина сублимированная, мальтит", benefit = "Без сахара, подходит для диабетиков"),
            ProductSeed("So crispy черника, кешью, арахис", "Злаковые батончики", 432, 7.0, 16.0, 60.0, 40, 79, listOf("nut"), nov = true,
                sostav = "Злаки, черника, кешью, арахис", benefit = "Источник белка и орехов"),
            ProductSeed("So crispy курага, миндаль, арахис", "Злаковые батончики", 436, 8.0, 17.0, 58.0, 40, 79, listOf("nut"), nov = true,
                sostav = "Злаки, курага, миндаль, арахис", benefit = "Источник калия и белка"),
            ProductSeed("ХитМикс мультизлаки, шоколад", "Злаковые батончики", 450, 7.0, 18.0, 64.0, 35, 55,
                sostav = "Мультизлаки, шоколадная глазурь", benefit = "Сытный перекус"),

            // Фруктовый грильяж
            ProductSeed("So fruity", "Фруктовый грильяж", 450, 7.0, 19.0, 62.0, 40, 70,
                sostav = "Фруктовое пюре, орехи, семечки", benefit = "Натуральные фрукты"),
            ProductSeed("So cherry", "Фруктовый грильяж", 450, 7.0, 19.0, 62.0, 40, 70,
                sostav = "Вишнёвое пюре, орехи, семечки", benefit = "Антиоксиданты"),
            ProductSeed("So mango", "Фруктовый грильяж", 448, 7.0, 19.0, 61.0, 40, 70, nov = true,
                sostav = "Манговое пюре, орехи, семечки", benefit = "Витамин A"),

            // Ореховые батончики
            ProductSeed("Орёл и Решка: арахис, солёный миндаль", "Ореховые батончики", 500, 13.0, 29.0, 47.0, 45, 95, listOf("nut", "prot"), hit = true,
                sostav = "Арахис, миндаль солёный, глюкозный сироп", benefit = "Высокое содержание белка"),
            ProductSeed("Орёл и Решка: арахис, малина, фисташка", "Ореховые батончики", 498, 12.0, 28.0, 50.0, 45, 95, listOf("nut"),
                sostav = "Арахис, фисташка, малина сублимированная", benefit = "Источник полезных жиров"),
            ProductSeed("Орёл и Решка: миндаль, тыквенные семечки", "Ореховые батончики", 512, 14.0, 31.0, 44.0, 45, 99, listOf("nut", "prot"), nov = true,
                sostav = "Миндаль, тыквенные семечки, мёд", benefit = "Цинк и магний"),
            ProductSeed("Орёл и Решка: арахис, миндаль, клюква", "Ореховые батончики", 498, 12.0, 28.0, 50.0, 45, 95, listOf("nut"),
                sostav = "Арахис, миндаль, клюква сушёная", benefit = "Витамин E"),

            // Протеиновые батончики
            ProductSeed("PROBATON «кокос-манго»", "Протеиновые батончики", 360, 30.0, 10.0, 38.0, 40, 120, listOf("prot"), nov = true,
                sostav = "Изолят сывороточного белка, кокос, манго", benefit = "30 г белка в одном батончике"),
            ProductSeed("PROBATON «шоколадно-малиновый брауни»", "Протеиновые батончики", 362, 30.0, 11.0, 36.0, 40, 120, listOf("prot"), nov = true,
                sostav = "Изолят сывороточного белка, какао, малина", benefit = "30 г белка, низкий сахар"),

            // Мармелад
            ProductSeed("Мармелад «Желейный формовой»", "Мармелад", 300, 0.0, 0.0, 80.0, 30, 45, listOf("fat"),
                sostav = "Сахар, патока, пектин, лимонная кислота", benefit = "Без жира"),
            ProductSeed("Мармелад «Ассорти»", "Мармелад", 300, 0.0, 0.0, 80.0, 30, 45, listOf("fat"),
                sostav = "Сахар, патока, пектин, натуральные красители", benefit = "Без жира"),
            ProductSeed("Мармелад «Мармедольки»", "Мармелад", 310, 0.0, 0.0, 80.0, 30, 50, listOf("fat"),
                sostav = "Сахар, патока, пектин, фруктовые соки", benefit = "Без жира, натуральные соки"),

            // Желейные конфеты
            ProductSeed("«Мир желе» микс", "Желейные конфеты", 380, 1.5, 8.0, 80.0, 25, 40,
                sostav = "Сахар, патока, желатин, растительный жир", benefit = "Лёгкий перекус"),
            ProductSeed("«Фруджио»", "Желейные конфеты", 292, 0.0, 0.0, 73.0, 25, 45, listOf("fat"),
                sostav = "Сахар, пектин, фруктовое пюре", benefit = "Без жира, натуральное пюре"),
            ProductSeed("«Цитрон»", "Желейные конфеты", 380, 0.5, 6.5, 80.0, 25, 40,
                sostav = "Сахар, патока, желатин, цитрусовые ароматизаторы", benefit = "Освежающий вкус"),
            ProductSeed("«Пожужжим» микс", "Желейные конфеты", 359, 1.4, 8.0, 69.0, 25, 40,
                sostav = "Сахар, патока, желатин, растительный жир", benefit = "Весёлая форма"),
            ProductSeed("«Фрунтик» микс", "Желейные конфеты", 350, 1.0, 7.0, 72.0, 25, 40,
                sostav = "Сахар, патока, желатин, фруктовые соки", benefit = "Натуральные соки")
        )

        products.forEach { p ->
            val catId = categoryIds[p.cat]!!
            val productId = Products.insert {
                it[categoryId] = catId
                it[name] = p.name
                it[kcal] = p.kcal
                it[protein] = p.p
                it[fat] = p.f
                it[carb] = p.c
                it[grams] = p.g
                it[price] = p.price
                it[sostav] = p.sostav
                it[benefit] = p.benefit
                it[isHit] = p.hit
                it[isNovelty] = p.nov
            } get Products.id

            p.tags.forEach { tag ->
                ProductTags.insert {
                    it[ProductTags.productId] = productId
                    it[ProductTags.tag] = tag
                }
            }
        }

        // Промокоды
        PromoCodes.insert {
            it[code] = "ЗОЖ10"
            it[discountPercent] = 10
            it[active] = true
        }
        PromoCodes.insert {
            it[code] = "ДРУГ500"
            it[discountPercent] = 0
            it[active] = true
        }

        // Пункты самовывоза
        PickupPoints.insert {
            it[name] = "Невский Кондитер — Невский пр."
            it[address] = "Санкт-Петербург, Невский пр., 100"
            it[lat] = 59.93
            it[lng] = 30.36
        }
        PickupPoints.insert {
            it[name] = "Невский Кондитер — Лиговский"
            it[address] = "Санкт-Петербург, Лиговский пр., 50"
            it[lat] = 59.92
            it[lng] = 30.35
        }

        // Демо-пользователи
        val adminHash = BCrypt.hashpw("admin1234", BCrypt.gensalt(10))
        Users.insert {
            it[email] = "admin@nk.ru"
            it[passwordHash] = adminHash
            it[name] = "Администратор фабрики"
            it[role] = "admin"
            it[points] = 0
        }

        val demoHash = BCrypt.hashpw("demo1234", BCrypt.gensalt(10))
        Users.insert {
            it[email] = "demo@nk.ru"
            it[passwordHash] = demoHash
            it[name] = "Демо-пользователь"
            it[sex] = "m"
            it[age] = 30
            it[height] = 180
            it[weight] = 80
            it[activity] = 1.375
            it[goal] = "keep"
            it[kcalNorm] = 2400
            it[points] = 250
            it[role] = "user"
        }

        log.info("Демо-данные загружены: ${products.size} продуктов, 6 категорий, 2 пользователя")
    }
}
