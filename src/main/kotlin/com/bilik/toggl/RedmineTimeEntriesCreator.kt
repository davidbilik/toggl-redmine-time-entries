package com.bilik.toggl

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.time.format.DateTimeFormatter

/**
 * Creator of new time entries in Redmine
 */
class RedmineTimeEntriesCreator(
    private val apiKey: String,
    private val redmineUrl: String
) {

    private val httpClient by lazy { getOkHttpClient() }
    private val gson = GsonBuilder().create()
    private val apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    suspend fun createNewEntries(entries: List<TimeEntry>): List<TimeEntry> {
        val successes = mutableListOf<TimeEntry>()
        entries.forEach { entry ->
            try {
                val urlString = "$redmineUrl${if (redmineUrl.endsWith("/")) "" else "/"}time_entries.json"
                val request = NewTimeEntryRequest(
                    NewTimeEntry(
                        entry.issueId,
                        hours = entry.duration.toMinutes() / 60.0f,
                        spent_on = entry.date.format(apiDateFormatter)
                    )
                )
                val call = httpClient.newCall(
                    Request.Builder()
                        .url(urlString)
                        .header("X-Redmine-API-Key", apiKey)
                        .post(gson.toJson(request).toRequestBody("application/json".toMediaType()))
                        .build()
                )
                val response = withContext(Dispatchers.IO) {
                    call.execute()
                }
                if (!response.isSuccessful) {
                    throw HttpException(response.code, response.body?.string() ?: "")
                }
                successes.add(entry)
            } catch (e: Exception) {
                println("Error while creating entry $entry")
                println(e)
            }
        }
        return successes
    }
}

private data class NewTimeEntry(
    val issue_id: String,
    val hours: Float,
    val activity_id: String = "9",
    val spent_on: String
)

private data class NewTimeEntryRequest(val time_entry: NewTimeEntry)

data class HttpException(val code: Int, val body: String) : Exception()