package org.clem0908.localmusicblindtest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Query("SELECT * FROM music_folders")
    suspend fun getAll(): List<FolderEntity>

    @Query("DELETE FROM music_folders")
    suspend fun deleteAll()

    @Query("DELETE FROM music_folders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
