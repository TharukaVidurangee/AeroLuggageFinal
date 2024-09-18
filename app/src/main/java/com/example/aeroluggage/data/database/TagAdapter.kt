package com.example.aeroluggage

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.aeroluggage.data.database.TagDatabaseHelper
import com.example.aeroluggage.data.models.SyncData
import com.example.aeroluggage.data.models.Tag
import com.example.aeroluggage.domain.storage.StorageRoom
import com.example.aeroluggage.sync.SyncCallback
import com.example.aeroluggage.sync.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Adapter class for displaying tags in a RecyclerView
class TagAdapter(

    private var tags: List<Tag>,  // List of Tag objects
    private val context: Context   // Context for Toasts and API calls
) : RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    private var db: TagDatabaseHelper = TagDatabaseHelper(context)
    private val halfScreenWidth: Float
    private var swipedPosition: Int = RecyclerView.NO_POSITION // Track the swiped item position

    init {
        // Get the display metrics to calculate half of the screen width
        val displayMetrics = context.resources.displayMetrics
        halfScreenWidth = displayMetrics.widthPixels / 2f
    }

    // ViewHolder for each tag item
    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val syncButton: ImageView = itemView.findViewById(R.id.syncButton)
        val tagTextView: TextView = itemView.findViewById(R.id.tagTextView)
        val roomTextView: TextView = itemView.findViewById(R.id.roomTextView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        val dateTimeTextView: TextView = itemView.findViewById(R.id.dateTimeTextView)
        val hiddenButtons: LinearLayout = itemView.findViewById(R.id.hidden_buttons)
        val cardView: View = itemView.findViewById(R.id.card_view)
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
        val tag = tags[holder.adapterPosition]
        holder.tagTextView.text = tag.bagtag
        holder.roomTextView.text = "Room ${tag.room}"
        holder.dateTimeTextView.text = tag.dateTime

        // Initially hide the buttons and reset the position if this item was swiped before
        if (swipedPosition == holder.adapterPosition) {
            holder.hiddenButtons.visibility = View.VISIBLE
            holder.cardView.translationX = -halfScreenWidth
            holder.hiddenButtons.alpha = 1f
            holder.dateTimeTextView.alpha = 0f
        } else {
            holder.hiddenButtons.visibility = View.INVISIBLE
            holder.cardView.translationX = 0f
            holder.hiddenButtons.alpha = 0f
            holder.dateTimeTextView.alpha = 1f
        }

        // Initialize gesture detector to handle swipe gestures
        val gestureDetector = GestureDetector(holder.itemView.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (e1 != null && e2 != null) {
                    val dx = e2.x - e1.x
                    if (dx < 0) { // Swiping left to reveal buttons
                        holder.hiddenButtons.visibility = View.VISIBLE
                        holder.cardView.translationX = dx.coerceAtLeast(-halfScreenWidth) // Limit translation to half screen width

                        val alpha = Math.min(1f, Math.abs(dx) / halfScreenWidth)
                        holder.hiddenButtons.alpha = alpha // Set alpha to buttons

                        // Fade out dateTimeTextView as you swipe left
                        holder.dateTimeTextView.alpha = 1f - alpha
                    }
                }
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null && e2 != null) {
                    if (e1.x > e2.x) { // Fling left to fully reveal buttons
                        holder.cardView.animate().translationX(-halfScreenWidth).setDuration(300).withEndAction {
                            holder.hiddenButtons.visibility = View.VISIBLE
                        }.start() // Limit fling to half screen width
                        holder.hiddenButtons.alpha = 1f
                        holder.dateTimeTextView.animate().alpha(0f).setDuration(300).start()
                        // Set the swiped position
                        if (swipedPosition != holder.adapterPosition) {
                            notifyItemChanged(swipedPosition)
                            swipedPosition = holder.adapterPosition
                        }
                    } else if (e1.x < e2.x) { // Fling right to hide buttons
                        resetCardPosition(holder) // Reset the card position and hide the buttons
                    }
                }
                return true
            }
        })

        // Set the touch listener to the itemView
        holder.itemView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (holder.cardView.translationX != 0f) {
                        resetCardPosition(holder) // Reset the card position
                    }
                }
            }
            gestureDetector.onTouchEvent(event)
            true
        }

        // Handle syncButton click event
        holder.syncButton.setOnClickListener {
            if (holder.hiddenButtons.visibility == View.VISIBLE) {
                syncTag(tag) // Sync the specific tag
            }
        }

        // Handle deleteButton click event
        holder.deleteButton.setOnClickListener {
            if (holder.hiddenButtons.visibility == View.VISIBLE) {
                db.deleteTag(tag.id)
                refreshData(db.getUnsyncedTags(tag.room)) // Fetch sorted unsynced tags after deletion
                Toast.makeText(holder.itemView.context, "Tag deleted", Toast.LENGTH_SHORT).show()
                swipedPosition = RecyclerView.NO_POSITION // Reset swiped position after deletion
            }
        }
    }

    private fun resetCardPosition(holder: TagViewHolder) {
        holder.cardView.animate().translationX(0f).setDuration(300).withEndAction {
            holder.hiddenButtons.alpha = 0f // Hide the buttons by setting alpha
            holder.hiddenButtons.visibility = View.INVISIBLE // Use INVISIBLE instead of GONE
            holder.dateTimeTextView.animate().alpha(1f).setDuration(300).start()
            if (swipedPosition == holder.adapterPosition) {
                swipedPosition = RecyclerView.NO_POSITION // Reset the swiped position
            }
        }.start()
    }

    // Refresh the data in the adapter
    fun refreshData(newTags: List<Tag>) {
        tags = newTags.sortedBy { it.room }
        notifyDataSetChanged()
        swipedPosition = RecyclerView.NO_POSITION // Reset swiped position after data refresh
    }

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
                Toast.makeText(context, "Tag synced", Toast.LENGTH_SHORT).show()
                db.markAsSynced(tag.id.toString()) // Mark the tag as synced in the database
                removeTagFromList(tag) // Remove the synced tag from the list and refre
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

        // After syncing all tags, refresh the data to show only unsynced tags
        refreshData(db.getUnsyncedTags(room = toString()))
    }

    private fun removeTagFromList(tag: Tag) {
        val updatedTags = tags.toMutableList() // Create a mutable copy of the tags list
        updatedTags.remove(tag) // Remove the synced tag from the list
        refreshData(updatedTags) // Update the adapter's data and refresh the RecyclerView
    }
}
