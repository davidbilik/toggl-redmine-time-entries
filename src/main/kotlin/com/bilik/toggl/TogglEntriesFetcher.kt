package com.bilik.toggl

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Fetcher for summarized entries from Toggl
 */
class TogglEntriesFetcher(
    private val togglApi: TogglApi,
    private val email: String,
    // TODO use
    private val bottomThresholdDate: LocalDate
) {

    suspend fun getEntries(): List<TimeEntry> {
        val entries = readAllTimeEntries()
        val groupedByDate = entries.groupBy { it.start.toLocalDate() }
        val groupedByTitleInDay = groupedByDate.mapValues { (date, entriesInDay) ->
            entriesInDay.groupBy {
                it.title
            }.mapValues { (title, items) ->
                val matcher = "[#]{0,1}[0-9]+".toPattern().matcher(title)
                val issueId = if (matcher.find()) {
                    matcher.group(0).replace("#", "")
                } else {
                    println("Add issue id for an entry '$title':")
                    Scanner(System.`in`).nextLine()
                }
                val issueDescription = title.dropWhile { it == '#' || it.isDigit() }
                TimeEntry(
                    items.map { it.id },
                    issueId,
                    items.fold(Duration.ZERO) { acc, toggleTimeEntry -> acc.plus(toggleTimeEntry.duration) },
                    date = date,
                    description = issueDescription
                )
            }.values
        }
        return groupedByTitleInDay.values.flatten()
    }

    private suspend fun readAllTimeEntries(): List<ToggleTimeEntry> {
        val since = LocalDate.now().minusMonths(1)
        val until = LocalDate.now()
        val result = mutableListOf<ToggleTimeEntry>()
        var page = 1
        var totalCount: Int
        do {
            val response = togglApi.getTimeEntries(email, since = since, until = until, page = page)
            totalCount = response.total_count
            result += response.data.map { apiItem ->
                ToggleTimeEntry(
                    id = apiItem.id,
                    title = apiItem.description,
                    duration = Duration.ofMillis(apiItem.dur),
                    start = LocalDateTime.parse(apiItem.start, DateTimeFormatter.ISO_DATE_TIME),
                    end = LocalDateTime.parse(apiItem.end, DateTimeFormatter.ISO_DATE_TIME)
                )
            }
            page++
        } while (result.size < totalCount)
        return result
    }
}

data class ToggleTimeEntry(
    val id: String,
    val title: String,
    val duration: Duration,
    val start: LocalDateTime,
    val end: LocalDateTime
)