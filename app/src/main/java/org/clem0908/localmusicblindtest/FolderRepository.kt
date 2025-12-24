package org.clem0908.localmusicblindtest

import android.content.Context

class FolderRepository(context: Context) {

    private val folderDao =
        AppDatabase.getInstance(context).folderDao()

    suspend fun insert(folder: FolderEntity) {
        folderDao.insert(folder)
    }

    suspend fun getAll(): List<FolderEntity> {
        return folderDao.getAll()
    }

    suspend fun deleteAll() {
        folderDao.deleteAll()
    }

    suspend fun deleteById(id: Long) {
        folderDao.deleteById(id)
    }
}
