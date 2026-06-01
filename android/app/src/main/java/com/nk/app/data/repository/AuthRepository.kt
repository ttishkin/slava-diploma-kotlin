package com.nk.app.data.repository

import com.nk.app.data.api.NkApi
import com.nk.app.data.local.TokenStore
import com.nk.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: NkApi,
    private val tokenStore: TokenStore
) {
    suspend fun login(email: String, password: String): AuthResponse {
        val response = api.login(LoginRequest(email, password))
        tokenStore.save(response.token)
        return response
    }

    suspend fun register(req: RegisterRequest): AuthResponse {
        val response = api.register(req)
        tokenStore.save(response.token)
        return response
    }

    suspend fun getProfile(): User = api.getProfile()

    suspend fun logout() {
        tokenStore.clear()
    }

    suspend fun isLoggedIn(): Boolean = tokenStore.getToken() != null
}
