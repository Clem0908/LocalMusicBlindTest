package org.clem0908.localmusicblindtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class Database : AppCompatActivity() {

    private lateinit var repository: FolderRepository
    private lateinit var adapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.database)

        repository = FolderRepository(this)

        val recyclerView = findViewById<RecyclerView>(R.id.folder_list)
        adapter = FolderAdapter(emptyList()) { _ -> }

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
}