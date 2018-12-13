import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import java.io.FileReader


fun main(args: Array<String>) {
    runBlocking {
        val config = Gson().fromJson<AppConfig>(
            FileReader(
                args.getOrNull(0) ?: "config.json"
            ), AppConfig::class.java
        )

        val togglFetcher = TogglEntriesFetcher(
            apiToken = config.toggl_apikey,
            email = config.email,
            workspaceId = config.workspace_id,
            date = config.date
        )
        val redmineCreator = RedmineTimeEntriesCreator(
            apiKey = config.redmine_api_key,
            redmineUrl = config.redmine_base_url
        )
        redmineCreator.createNewEntries(togglFetcher.getEntries())
    }
}
