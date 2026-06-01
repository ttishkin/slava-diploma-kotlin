package com.nk.app.data.repository

import com.nk.app.data.api.NkApi
import com.nk.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(private val api: NkApi) {

    suspend fun getEntries(day: String): List<DiaryEntry> = api.getDiary(day)

    suspend fun addEntry(entry: CreateDiaryEntry): DiaryEntry = api.addDiaryEntry(entry)

    suspend fun updateEntry(id: Int, qty: Int): DiaryEntry =
        api.updateDiaryEntry(id, UpdateDiaryEntry(qty))

    suspend fun deleteEntry(id: Int) = api.deleteDiaryEntry(id)

    suspend fun getBonuses(): BonusResponse = api.getBonuses()

    suspend fun getFavorites(): List<Int> = api.getFavorites().productIds

    suspend fun updateFavorites(ids: List<Int>) =
        api.updateFavorites(FavoritesRequest(ids))
}
