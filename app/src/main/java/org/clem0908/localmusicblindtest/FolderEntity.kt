package org.clem0908.localmusicblindtest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val displayName: String
)
