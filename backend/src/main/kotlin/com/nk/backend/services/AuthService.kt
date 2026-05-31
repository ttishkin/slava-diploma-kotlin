package com.nk.backend.services

import com.nk.backend.models.*
import com.nk.backend.plugins.JwtConfig
import com.nk.backend.plugins.badRequest
import com.nk.backend.plugins.unauthorized
import com.nk.backend.repositories.UserRepository
import org.mindrot.jbcrypt.BCrypt

object AuthService {

    fun register(req: RegisterRequest): AuthResponse {
        // Валидация
        if (req.email.isBlank() || !req.email.contains("@") || !req.email.contains("."))
            badRequest("Некорректный email")
        if (req.password.length < 6)
            badRequest("Пароль должен быть не менее 6 символов")

        // Проверка дубликата
        if (UserRepository.findByEmail(req.email) != null)
            badRequest("Пользователь с таким email уже зарегистрирован")

        // Расчёт калорийной нормы
        val kcalNorm = calculateKcalNorm(req.sex, req.age, req.height, req.weight, req.activity, req.goal)

        // Хешируем пароль
        val hash = BCrypt.hashpw(req.password, BCrypt.gensalt(10))

        val userId = UserRepository.create(
            email = req.email,
            passwordHash = hash,
            name = req.name,
            sex = req.sex,
            age = req.age,
            height = req.height,
            weight = req.weight,
            activity = req.activity,
            goal = req.goal,
            kcalNorm = kcalNorm
        )

        val user = UserRepository.findById(userId)!!
        val token = JwtConfig.generateToken(userId, user.role)

        return AuthResponse(token, UserRepository.toDto(user))
    }

    fun login(req: LoginRequest): AuthResponse {
        if (req.email.isBlank()) badRequest("Email обязателен")
        if (req.password.isBlank()) badRequest("Пароль обязателен")

        val user = UserRepository.findByEmail(req.email)
            ?: unauthorized("Неверный email или пароль")

        if (!BCrypt.checkpw(req.password, user.passwordHash))
            unauthorized("Неверный email или пароль")

        val token = JwtConfig.generateToken(user.id, user.role)
        return AuthResponse(token, UserRepository.toDto(user))
    }

    fun getProfile(userId: Int): UserDto {
        val user = UserRepository.findById(userId)
            ?: unauthorized("Пользователь не найден")
        return UserRepository.toDto(user)
    }

    // Формула Mifflin-St Jeor
    private fun calculateKcalNorm(
        sex: String?, age: Int?, height: Int?, weight: Int?,
        activity: Double?, goal: String?
    ): Int? {
        if (sex == null || age == null || height == null || weight == null) return null

        // BMR по Mifflin-St Jeor
        val bmr = if (sex == "m") {
            10.0 * weight + 6.25 * height - 5.0 * age + 5
        } else {
            10.0 * weight + 6.25 * height - 5.0 * age - 161
        }

        val activityMultiplier = activity ?: 1.375
        var tdee = bmr * activityMultiplier

        // Корректировка по цели
        when (goal) {
            "lose" -> tdee *= 0.85
            "gain" -> tdee *= 1.15
        }

        return tdee.toInt()
    }
}
