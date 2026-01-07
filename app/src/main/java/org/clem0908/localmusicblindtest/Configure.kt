package org.clem0908.localmusicblindtest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Configure : AppCompatActivity() {

    private lateinit var repository: FolderRepository

    private val folderPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri?.let {
                lifecycleScope.launch { addFolder(it) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configure)

        repository = FolderRepository(this)

        findViewById<Button>(R.id.database).setOnClickListener {
            startActivity(Intent(this, Database::class.java))
        }

        findViewById<Button>(R.id.add_folder).setOnClickListener {
            folderPicker.launch(null)
        }

        findViewById<Button>(R.id.remove_folder).setOnClickListener {
            startActivity(Intent(this, RemoveFolderActivity::class.java))
        }

        findViewById<Button>(R.id.purge_database).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.purge_database)
            builder.setMessage(R.string.are_you_sure)
            builder.setPositiveButton(R.string.yes) { _, _ ->
                lifecycleScope.launch { repository.deleteAll() }
            }
            builder.setNegativeButton(R.string.no, null)
            builder.show()
        }
    }

    private suspend fun addFolder(uri: Uri) {
        withContext(Dispatchers.IO) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val folderName = DocumentFile.fromTreeUri(this@Configure, uri)?.name ?: R.string.unknown.toString()

            repository.insert(FolderEntity(uri = uri.toString(), displayName = folderName))
            Log.i("Add Folder", "Name=$folderName URI=$uri")
        }
    }
}