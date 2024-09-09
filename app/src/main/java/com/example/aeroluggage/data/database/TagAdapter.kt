//package com.example.aeroluggage.data.database
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.recyclerview.widget.RecyclerView
//import com.example.aeroluggage.R
//import com.example.aeroluggage.domain.storage.StorageRoom
//import com.example.aeroluggage.sync.SyncCallback
//import com.example.aeroluggage.sync.SyncManager
//import com.example.aeroluggage.data.models.SyncData
//import com.example.aeroluggage.data.models.Tag
//
//// Adapter class for displaying tags in a RecyclerView
//class TagAdapter(
//    private var tags: List<Tag>,  // List of Tag objects
//    private val context: Context   // Context for Toasts and API calls
//) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {
//
//    private var db: TagDatabaseHelper = TagDatabaseHelper(context)
//
//    // ViewHolder for each tag item
//    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val syncButton: ImageView = itemView.findViewById(R.id.syncButton)
//        val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
//        val roomTextView: TextView = itemView.findViewById(R.id.roomTextView)
//        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
//        val dateTimeTextView: TextView = itemView.findViewById(R.id.dateTimeTextView)
//    }
//
//    // Called when RecyclerView needs a new ViewHolder
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.tag_item, parent, false)
//        return TagViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = tags.size
//
//    // Bind the data to the ViewHolder
//    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
//        val tag = tags[position]
//        holder.tagTextView.text = tag.bagtag
//        holder.roomTextView.text = "Room ${tag.room}"
//        holder.dateTimeTextView.text = tag.dateTime
//
//        // Handle delete button click
//        holder.deleteButton.setOnClickListener {
//            db.deleteTag(tag.id)
//            refreshData(db.getAllTags())
//            Toast.makeText(holder.itemView.context, "Tag deleted", Toast.LENGTH_SHORT).show()
//        }
//
//        // Handle sync button click
//        holder.syncButton.setOnClickListener {
//            syncTag(tag) // Sync the specific tag
//            Toast.makeText(holder.itemView.context, "Tag synced", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun syncTag(tag: Tag) {
//        // Create a SyncManager instance and trigger sync for the specific tag
//        val syncData = SyncData(
//            StorageRoom = StorageRoom(tag.room), // Assuming StorageRoom can be initialized with room
//            BagTag = tag.bagtag,
//            AddedUser = tag.userID,
//            AddedDate = tag.dateTime
//        )
//
//        val syncManager = SyncManager(context, db)
//
//        // Remove the tag from the list after successful sync
//        syncManager.syncSingleTag(syncData, object : SyncCallback {
//            override fun onSyncSuccess() {
//                // Remove the tag from the list and refresh the RecyclerView
//                removeTagFromList(tag)
//            }
//
//            override fun onSyncFailure() {
//                // Handle failure if needed
//            }
//        })
//    }
//
//    fun syncAllTags() {
//        val tagsToSync = tags.toList() // Make a copy of the current list
//        for (tag in tagsToSync) {
//            syncTag(tag)
//        }
//    }
//
//    private fun removeTagFromList(tag: Tag) {
//        val updatedTags = tags.toMutableList() // Create a mutable copy of the tags list
//        updatedTags.remove(tag) // Remove the synced tag from the list
//        refreshData(updatedTags) // Update the adapter's data and refresh the RecyclerView
//    }
//
//    // Refresh the data in the adapter
//    fun refreshData(newTags: List<Tag>) {
//        tags = db.getUnsyncedTags()         //Ensure only fetch unsynced tags here
//        //getUnsyncedTags()
//        notifyDataSetChanged()
//    }
//}
//



package com.example.aeroluggage.data.database

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.aeroluggage.R
import com.example.aeroluggage.domain.storage.StorageRoom
import com.example.aeroluggage.sync.SyncCallback
import com.example.aeroluggage.sync.SyncManager
import com.example.aeroluggage.data.models.SyncData
import com.example.aeroluggage.data.models.Tag

// Adapter class for displaying tags in a RecyclerView
class TagAdapter(
    private var tags: List<Tag>,  // List of Tag objects
    private val context: Context   // Context for Toasts and API calls
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    private val db: TagDatabaseHelper = TagDatabaseHelper(context)

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_item, parent, false)
        return TagViewHolder(view)
    }

    override fun getItemCount(): Int = tags.size

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.tagTextView.text = tag.bagtag
        holder.roomTextView.text = "Room ${tag.room}"
        holder.dateTimeTextView.text = tag.dateTime

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            db.deleteTag(tag.id)
            refreshData(db.getUnsyncedTags()) // Fetch only unsynced tags after deletion
            Toast.makeText(holder.itemView.context, "Tag deleted", Toast.LENGTH_SHORT).show()
        }

        // Handle sync button click
        holder.syncButton.setOnClickListener {
            syncTag(tag) // Sync the specific tag
        }
    }

    // Method to sync a single tag
    private fun syncTag(tag: Tag) {
        val syncData = SyncData(
            StorageRoom = StorageRoom(tag.room), // Assuming StorageRoom can be initialized with room
            BagTag = tag.bagtag,
            AddedUser = tag.userID,
            AddedDate = tag.dateTime
        )

        val syncManager = SyncManager(context, db)

        syncManager.syncSingleTag(syncData, object : SyncCallback {
            override fun onSyncSuccess() {
                Toast.makeText(context, "Tag synced", Toast.LENGTH_SHORT).show()
                removeTagFromList(tag) // Remove the synced tag from the list and refresh the RecyclerView
            }

            override fun onSyncFailure() {
                Toast.makeText(context, "Sync failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Method to sync all tags in the list
    fun syncAllTags() {
        val tagsToSync = tags.toList() // Make a copy of the current list to avoid modification issues
        for (tag in tagsToSync) {
            syncTag(tag)
        }
    }

    // Method to remove a synced tag from the list and refresh the data
    private fun removeTagFromList(tag: Tag) {
        val updatedTags = tags.toMutableList() // Create a mutable copy of the tags list
        updatedTags.remove(tag) // Remove the synced tag from the list
        refreshData(updatedTags) // Update the adapter's data and refresh the RecyclerView
    }

    // Refresh the data in the adapter
    fun refreshData(newTags: List<Tag>) {
        tags = newTags // Update the list with the new data
        notifyDataSetChanged() // Notify the adapter to refresh the RecyclerView
    }
}

