package org.clem0908.localmusicblindtest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(
    private var folders: List<FolderEntity>,
    private val onLongClick: (FolderEntity) -> Unit // callback for long click
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.folder_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.name.text = folder.displayName

        // set long click listener
        holder.itemView.setOnLongClickListener {
            onLongClick(folder)
            true
        }
    }

    override fun getItemCount(): Int = folders.size

    fun updateData(newFolders: List<FolderEntity>) {
        folders = newFolders
        notifyDataSetChanged()
    }
}