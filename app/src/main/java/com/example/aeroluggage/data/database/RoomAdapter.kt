package com.example.aeroluggage.data.database

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aeroluggage.R
import com.example.aeroluggage.data.models.Room
import com.example.aeroluggage.ui.screens.TagListActivity

// Adapter class for the RecyclerView
class RoomAdapter : ListAdapter<Room, RoomAdapter.RoomViewHolder>(RoomDiffCallback()) {

    // onCreateViewHolder is called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        // Inflate the room_item.xml layout and create a RoomViewHolder with it
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.room_item, parent, false)
        return RoomViewHolder(view)
    }

    // onBindViewHolder binds the data to the ViewHolder for a specific position
    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        // Get the room object at the current position
        val room = getItem(position)
        // Bind the room data to the views
        holder.bind(room)
    }

    // RoomViewHolder holds the views for each item in the RecyclerView
    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomTextView: TextView = itemView.findViewById(R.id.roomTextView)
        private val tagCountTextView: TextView = itemView.findViewById(R.id.tagCountTextView)

        // Bind function to set data to the views
        fun bind(room: Room) {
            roomTextView.text = "Room ${room.roomNumber}"
            tagCountTextView.text = "Tag Count: ${room.tagCount}"

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, TagListActivity::class.java).apply {
                    putExtra("ROOM_NUMBER", room.roomNumber)
                }
                context.startActivity(intent)
            }

            // Set click listener for the room item
//            itemView.setOnClickListener {
//                // On click, start TagListActivity and pass the room number as an extra
//                val intent = Intent(itemView.context, TagListActivity::class.java).apply {
//                    putExtra("ROOM_NUMBER", room.roomNumber)
//                }
//                itemView.context.startActivity(intent)
//            }
        }
    }
}

// RoomDiffCallback is used to optimize the updating of the RecyclerView
class RoomDiffCallback : DiffUtil.ItemCallback<Room>() {
    // Check if two Room items represent the same data
    override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean {
        return oldItem.roomNumber == newItem.roomNumber
    }

    // Check if the content of two Room items is the same
    override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean {
        return oldItem == newItem
    }
}
