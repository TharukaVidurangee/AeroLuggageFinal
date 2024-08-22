package com.example.aeroluggage

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Adapter class for displaying tags in a RecyclerView
class TagAdapter(
    private var tags: List<Tag>,  // List of Tag objects
    private val context: Context   // Context for Toasts and API calls
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    private var db: TagDatabaseHelper = TagDatabaseHelper(context)

    // ViewHolder for each tag item
    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val syncButton: ImageView = itemView.findViewById(R.id.syncButton)
        val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
        val roomTextView: TextView = itemView.findViewById(R.id.roomTextView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val dateTimeTextView: TextView = itemView.findViewById(R.id.dateTimeTextView)
    }

    // Called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tag_item, parent, false)
        return TagViewHolder(view)
    }

    override fun getItemCount(): Int = tags.size

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.tagTextView.text = tag.bagtag
        holder.roomTextView.text = tag.room
        holder.dateTimeTextView.text = tag.dateTime

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            db.deleteTag(tag.id)
            refreshData(db.getAllTags())
            Toast.makeText(holder.itemView.context, "Tag deleted", Toast.LENGTH_SHORT).show()
        }

        // Handle sync button click
        holder.syncButton.setOnClickListener {
            syncTag(tag) // Sync the specific tag
        }
    }

    //method to remove a synced tag
//    private fun removeTag(tag: Tag) {
//        val position = tags.indexOf(tag)
//        if (position != -1) {
//            tags.removeAt(position)
//            notifyItemRemoved(position)  // Notify RecyclerView that the item was removed
//        }
//    }


    private fun syncTag(tag: Tag) {
        // Create a SyncManager instance and trigger sync for the specific tag
        val syncData = SyncData(
            StorageRoom = StorageRoom(tag.room), // Assuming StorageRoom can be initialized with room
            BagTag = tag.bagtag,
            AddedUser = tag.userID,
            AddedDate = tag.dateTime
        )

        val syncManager = SyncManager(context, db)

        // Remove the tag from the list after successful sync
        syncManager.syncSingleTag(syncData, object : SyncCallback {
            override fun onSyncSuccess() {
                // Remove the tag from the list and refresh the RecyclerView
                removeTagFromList(tag)
            }

            override fun onSyncFailure() {
                // Handle failure if needed
            }
        })
    }

    private fun removeTagFromList(tag: Tag) {
        val updatedTags = tags.toMutableList() // Create a mutable copy of the tags list
        updatedTags.remove(tag) // Remove the synced tag from the list
        refreshData(updatedTags) // Update the adapter's data and refresh the RecyclerView
    }

    // Refresh the data in the adapter
    fun refreshData(newTags: List<Tag>) {
        tags = newTags
        notifyDataSetChanged()
    }
}

