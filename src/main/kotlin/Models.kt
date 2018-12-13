// model classes

data class TimeEntry(val issueId: String, val timeMin: Long, val date: String)

data class AppConfig(
    val toggl_apikey: String,
    val workspace_id: String,
    val email: String,
    val redmine_api_key: String,
    val date: String?,
    val redmine_base_url: String
)