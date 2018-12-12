import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.basic.BasicAuth
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Fetcher for summarized entries from Toggl
 */
class TogglEntriesFetcher(
    private val apiToken: String,
    private val email: String,
    private val date: String?,
    private val workspaceId: String
) {

    suspend fun getEntries(): List<TimeEntry> {
        val client = HttpClient(Apache) {
            install(JsonFeature)
            install(BasicAuth) {
                username = apiToken
                password = "api_token"
            }
        }
        val response = client.get<TogglReportResponse>(urlString = "https://toggl.com/reports/api/v2/summary") {
            parameter("user_agent", email)
            parameter("workspace_id", workspaceId)
            parameter("since", date)
            parameter("until", date)
        }
        val entries = mutableListOf<TimeEntry>()
        response.data.forEach {
            it.items.forEach {
                val matcher = "[#]{0,1}[0-9]+".toPattern().matcher(it.title.time_entry)
                val issueId = if (matcher.find()) {
                    matcher.group(0)
                } else {
                    println("Add issue id for an entry '${it.title.time_entry}':")
                    Scanner(System.`in`).nextLine()
                }
                entries.add(TimeEntry(issueId, TimeUnit.MILLISECONDS.toMinutes(it.time)))
            }
        }
        return entries
    }
}


private data class TimeEntryTitle(val time_entry: String)

private data class TimeEntryItem(val title: TimeEntryTitle, val time: Long)

private data class TogglData(val items: List<TimeEntryItem>)

private data class TogglReportResponse(val data: List<TogglData>)

