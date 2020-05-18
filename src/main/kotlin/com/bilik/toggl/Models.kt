package com.bilik.toggl

import java.time.Duration
import java.time.LocalDate

// model classes

data class TimeEntry(
    val ids: List<String>,
    val issueId: String,
    val duration: Duration,
    val date: LocalDate
)

data class AppConfig(
    val toggl_apikey: String,
    val workspace_id: String,
    val email: String,
    val redmine_api_key: String,
    val redmine_base_url: String
)