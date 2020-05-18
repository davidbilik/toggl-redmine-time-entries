package com.bilik.toggl

class TogglTagger(
    private val togglApi: TogglApi
) {
    companion object {
        private const val TAG_NAME = "billed"
    }

    private suspend fun ensureTagExists() {
        try {
            togglApi.createTag(
                NewTagRequest(
                    tag = NewTag(
                        name = TAG_NAME,
                        wid = togglApi.workspaceId
                    )
                )
            )
            println("Tag created")
        } catch (e: Exception) {
            println("Tag exist")
        }
    }

    suspend fun assignTag(entries: List<TimeEntry>) {
        ensureTagExists()
        togglApi.assignTagsToTimeEntries(listOf(TAG_NAME), entries.map { it.ids }.flatten())
    }
}