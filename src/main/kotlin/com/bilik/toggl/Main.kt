package com.bilik.toggl

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import java.io.FileReader
import java.time.Duration
import java.time.LocalDate


fun main(args: Array<String>) {
    runBlocking {
        val config = readAppConfig(args)

        val togglApi = TogglApi(config.toggl_apikey, config.workspace_id)
        val togglTagger = TogglTagger(togglApi)
        val togglFetcher = TogglEntriesFetcher(
            togglApi,
            email = config.email,
            bottomThresholdDate = LocalDate.now().minusYears(1)
        )
        val redmineCreator = RedmineTimeEntriesCreator(
            apiKey = config.redmine_api_key,
            redmineUrl = config.redmine_base_url
        )

        val entries = togglFetcher.getEntries()
        val successfulEntries = redmineCreator.createNewEntries(entries)
        togglTagger.assignTag(successfulEntries)
        val totalTimeReported = successfulEntries.fold(Duration.ZERO) { acc, entry -> acc.plus(entry.duration) }
        println("Reported: ${totalTimeReported.toHours()}h ${totalTimeReported.toMinutesPart()}m")
    }
}

private fun readAppConfig(args: Array<String>): AppConfig {
    return Gson().fromJson<AppConfig>(
        FileReader(
            args.getOrNull(0) ?: "config.json"
        ), AppConfig::class.java
    )
}
