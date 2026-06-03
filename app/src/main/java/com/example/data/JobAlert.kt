package com.example.data

data class JobAlert(
    val title: String,
    val dept: String,
    val posts: String,
    val date: String,
    val badge: String,
    val category: String
) {
    fun toBookmarkedJob(): BookmarkedJob {
        return BookmarkedJob(
            title = title,
            dept = dept,
            posts = posts,
            date = date,
            badge = badge,
            category = category
        )
    }
}
