package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM bookmarked_jobs ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedJob>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(job: BookmarkedJob)

    @Query("DELETE FROM bookmarked_jobs WHERE title = :title")
    suspend fun deleteBookmarkByTitle(title: String)

    @Query("SELECT EXISTS(SELECT * FROM bookmarked_jobs WHERE title = :title)")
    suspend fun exists(title: String): Boolean
}
