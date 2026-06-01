package com.nk.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "nk_prefs")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tokenKey = stringPreferencesKey("jwt_token")

    val token: Flow<String?> = context.dataStore.data.map { it[tokenKey] }

    suspend fun getToken(): String? = token.first()

    suspend fun save(token: String) {
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.remove(tokenKey) }
    }
}
