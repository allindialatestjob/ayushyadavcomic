package com.example.data

import kotlinx.coroutines.flow.Flow

class JobRepository(private val jobDao: JobDao) {
    val allBookmarks: Flow<List<BookmarkedJob>> = jobDao.getAllBookmarks()

    suspend fun insert(job: BookmarkedJob) {
        jobDao.insertBookmark(job)
    }

    suspend fun deleteByTitle(title: String) {
        jobDao.deleteBookmarkByTitle(title)
    }

    suspend fun isBookmarked(title: String): Boolean {
        return jobDao.exists(title)
    }
}
