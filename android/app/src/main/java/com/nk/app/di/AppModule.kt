package com.nk.app.di

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nk.app.data.api.NkApi
import com.nk.app.data.local.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val TAG = "AppModule"

    // Список адресов для поиска бэкенда
    private val CANDIDATE_URLS = listOf(
        "http://10.0.2.2:8080",   // Android эмулятор → host localhost
        "http://127.0.0.1:8080",  // localhost (на устройстве)
        "http://localhost:8080"    // localhost (альтернатива)
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Проверяет доступность сервера по TCP-соединению
     */
    private fun isHostReachable(host: String, port: Int, timeoutMs: Int = 1500): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(host, port), timeoutMs)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Находит первый доступный URL бэкенда
     */
    private fun discoverBaseUrl(): String {
        for (url in CANDIDATE_URLS) {
            try {
                val uri = java.net.URI(url)
                val host = uri.host
                val port = uri.port
                if (isHostReachable(host, port)) {
                    Log.i(TAG, "Бэкенд найден: $url")
                    return url
                }
            } catch (e: Exception) {
                Log.w(TAG, "Ошибка проверки $url: ${e.message}")
            }
        }
        // По умолчанию — эмулятор
        Log.w(TAG, "Бэкенд не найден, используем ${CANDIDATE_URLS[0]} по умолчанию")
        return CANDIDATE_URLS[0]
    }

    @Provides
    @Singleton
    fun provideOkHttp(tokenStore: TokenStore): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { tokenStore.getToken() }
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val baseUrl = discoverBaseUrl()
        return Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): NkApi {
        return retrofit.create(NkApi::class.java)
    }
}
