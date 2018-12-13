import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Creator of new time entries in Redmine
 */
class RedmineTimeEntriesCreator(
    private val apiKey: String,
    private val redmineUrl: String
) {

    suspend fun createNewEntries(entries: List<TimeEntry>) {
        val client = HttpClient(Apache) {
            install(JsonFeature)
        }
        entries.forEach { entry ->
            try {
                client.post<Unit>(urlString = "${redmineUrl}${if (redmineUrl.endsWith("/")) "" else "/"}time_entries.json") {
                    header("X-Redmine-API-Key", apiKey)
                    contentType(ContentType.parse("application/json"))
                    body = NewTimeEntryRequest(
                        NewTimeEntry(
                            entry.issueId,
                            entry.timeMin / 60.0f,
                            spent_on = entry.date
                        )
                    )
                }
            } catch (e: BadResponseStatusException) {
                println(e.response.readText())
            }
        }
    }

}

private data class NewTimeEntry(
    val issue_id: String,
    val hours: Float,
    val activity_id: String = "9",
    val spent_on: String
)

private data class NewTimeEntryRequest(val time_entry: NewTimeEntry)