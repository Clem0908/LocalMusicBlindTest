package org.clem0908.localmusicblindtest

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class RemoveFolderActivity : AppCompatActivity() {

    private lateinit var repository: FolderRepository
    private lateinit var adapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_folder)

        repository = FolderRepository(this)

        val recyclerView = findViewById<RecyclerView>(R.id.folder_list)

        adapter = FolderAdapter(emptyList()) { folder ->
            confirmDelete(folder)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadFolders()
    }

    private fun loadFolders() {
        lifecycleScope.launch {
            val folders = repository.getAll()
            adapter.updateData(folders)
        }
    }

    private fun confirmDelete(folder: FolderEntity) {
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_folder)
            .setMessage(folder.displayName)
            .setPositiveButton(R.string.yes) { _, _ ->
                lifecycleScope.launch {
                    repository.deleteById(folder.id)
                    loadFolders() // refresh list
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
