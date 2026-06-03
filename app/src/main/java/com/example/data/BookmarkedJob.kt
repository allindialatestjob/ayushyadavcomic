package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_jobs")
data class BookmarkedJob(
    @PrimaryKey val title: String,
    val dept: String,
    val posts: String,
    val date: String,
    val badge: String,
    val category: String,
    val bookmarkedAt: Long = System.currentTimeMillis()
)
