package com.bilik.toggl

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TogglApi(
    private val apiToken: String,
    val workspaceId: String
) {

    private val httpClient by lazy { getTogglApiClient() }
    private val gson = GsonBuilder().create()

    private fun getTogglApiClient(): OkHttpClient {
        val authInterceptor = getAuthInterceptor()
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private fun getAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder().header("Authorization", Credentials.basic(apiToken, "api_token"))
                    .build()
            )
        }
    }

    suspend fun getTimeEntries(
        email: String,
        until: LocalDate,
        since: LocalDate,
        page: Int
    ): DetailedReportResponse {
        val call = httpClient.newCall(
            Request.Builder()
                .get()
                .url(
                    "https://toggl.com/reports/api/v2/details".toHttpUrl().newBuilder()
                        .addQueryParameter("user_agent", email)
                        .addQueryParameter("workspace_id", workspaceId)
                        .addQueryParameter("until", until.formatForApi())
                        .addQueryParameter("since", since.formatForApi())
                        .addQueryParameter("tag_ids", "0")
                        .addQueryParameter("page", page.toString())
                        .build()
                )
                .build()
        )
        return withContext(Dispatchers.IO) {
            val response = call.execute()
            gson.fromJson(InputStreamReader(response.body!!.byteStream()), DetailedReportResponse::class.java)
        }
    }

    private fun LocalDate.formatForApi() = format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    suspend fun createTag(request: NewTagRequest) {
        val call = httpClient.newCall(
            Request.Builder()
                .url("https://www.toggl.com/api/v8/tags")
                .post(gson.toJson(request).toRequestBody("application/json".toMediaType()))
                .build()
        )

        withContext(Dispatchers.IO) {
            call.execute()
        }
    }

    suspend fun assignTagsToTimeEntries(tags: List<String>, ids: List<String>) {
        val request = AssignTagRequest(TimeEntryTags(tags = tags))
        ids.chunked(10).forEach { chunkedIds ->
            val call = httpClient.newCall(
                Request.Builder()
                    .url(
                        "https://www.toggl.com/api/v8/time_entries/".toHttpUrl().newBuilder()
                            .addPathSegment(chunkedIds.joinToString(","))
                            .build()
                    )
                    .put(gson.toJson(request).toRequestBody("application/json".toMediaType()))
                    .build()
            )

            withContext(Dispatchers.IO) {
                call.execute()
            }
        }
    }
}

data class TimeEntryItem(
    val id: String, val description: String, val dur: Long, val start: String, val end: String
)

data class NewTagRequest(val tag: NewTag)

data class NewTag(
    val name: String,
    val wid: String
)

data class AssignTagRequest(
    val time_entry: TimeEntryTags
)

data class TimeEntryTags(
    val tags: List<String>
)

data class DetailedReportResponse(
    val total_count: Int,
    val per_page: Int,
    val data: List<TimeEntryItem>
)