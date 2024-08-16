//package com.example.aeroluggage
//
//import android.content.Context
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.recyclerview.widget.RecyclerView
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class TagAdapter(private var tags: List<Tag>, context: Context) :
//    RecyclerView.Adapter<TagAdapter.TagViewHolder>() {
//
//    private var db: TagDatabaseHelper = TagDatabaseHelper(context)
//
//    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val syncButton: ImageView = itemView.findViewById(R.id.syncButton)
//        val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
//        val roomTextView: TextView = itemView.findViewById(R.id.roomTextView)
//        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
//        val dateTimeTextView: TextView = itemView.findViewById(R.id.dateTimeTextView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_item, parent, false)
//        return TagViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = tags.size
//
//    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
//        val tag = tags[position]
//        holder.tagTextView.text = tag.bagtag
//        holder.roomTextView.text = tag.room
//        holder.dateTimeTextView.text = tag.dateTime
//
//        holder.deleteButton.setOnClickListener {
//            db.deleteTag(tag.id)
//            refreshData(db.getAllTags())
//            Toast.makeText(holder.itemView.context, "Tag deleted", Toast.LENGTH_SHORT).show()
//        }
//
//        // Set an OnClickListener on the syncButton using view binding
//        holder.syncButton.setOnClickListener {
//            Log.d("SYNC_BUTTON", "Sync button clicked")
//            Toast.makeText(holder.itemView.context, "sync button clicked", Toast.LENGTH_SHORT).show()
//            triggerSync(holder.itemView.context)    // Pass the context to triggerSync
//        }
//    }
//
//    private fun triggerSync(context: Context) {
//        val unsyncedTags = db.getUnsyncedTags()
//        val jsonData = JsonUtils.convertToJSON(unsyncedTags)
//
//        // Log the JSON data for verification
//        Log.d("SYNC_DATA_JSON", jsonData)
//
//        // Call the API to sync the data
//        val call = RetrofitClient.instance.sendSyncData(unsyncedTags)
//        call.enqueue(object : Callback<SyncResponse> {
//            override fun onResponse(call: Call<SyncResponse>, response: Response<SyncResponse>) {
//                if (response.isSuccessful) {
//                    val syncResponse = response.body()
//                    val message = syncResponse?.message ?: "Sync successful"
//                    Log.d("SYNC_DEBUG", "Server message: $message")
//
//                    // Mark tags as synced in the local database
//                    unsyncedTags.forEach { tag ->
//                        db.markAsSynced(tag.BagTag)
//                    }
//
//                    // Show a toast with the server's response message
//                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                } else {
//                    Log.e("SYNC_DEBUG", "Failed to sync data: ${response.code()}")
//                    Toast.makeText(context, "Failed to sync data.", Toast.LENGTH_SHORT).show()
//                }
//            }
//            override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
//                Log.e("SYNC_DEBUG", "Sync failed: ${t.message}")
//                Toast.makeText(context, "Sync failed: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    fun refreshData(newTags: List<Tag>) {
//        tags = newTags
//        notifyDataSetChanged()
//    }
//}


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
            Log.d("SYNC_BUTTON", "Sync button clicked")
//            Toast.makeText(holder.itemView.context, "Sync button clicked", Toast.LENGTH_SHORT).show()
            triggerSync(holder.itemView.context)
        }
    }

    private fun triggerSync(context: Context) {
        // Fetch unsynced tags from the local database
        val unsyncedTags = db.getUnsyncedTags()

        // Convert the list of unsynced tags to JSON
        val jsonData = JsonUtils.convertToJSON(unsyncedTags)

        // Log the JSON data for verification (optional)
        Log.d("SYNC_DATA_JSON", jsonData)

        // Make the API call to send the data to the server
        val call = RetrofitClient.instance.sendSyncData(unsyncedTags)
        call.enqueue(object : Callback<SyncResponse> {
            override fun onResponse(call: Call<SyncResponse>, response: Response<SyncResponse>) {
                if (response.isSuccessful) {
                    val syncResponse = response.body()
                    val message = syncResponse?.ReturnCode ?: "Sync successful"
                    Log.d("SYNC_DEBUG", "Server message: $message")

                    // Mark tags as synced in the local database
                    unsyncedTags.forEach { tag ->
                        db.markAsSynced(tag.BagTag)
                    }

                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("SYNC_DEBUG", "Failed to sync data: ${response.code()}")
                    Toast.makeText(context, "Failed to sync data.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                Log.e("SYNC_DEBUG", "Sync failed: ${t.message}")
                Toast.makeText(context, "Sync failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Refresh the data in the adapter
    fun refreshData(newTags: List<Tag>) {
        tags = newTags
        notifyDataSetChanged()
    }
}

