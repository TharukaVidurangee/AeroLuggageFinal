package com.example.aeroluggage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RoomHistoryScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_history_screen)

        // Step 1: Set up the RecyclerView
        val roomRecyclerView: RecyclerView = findViewById(R.id.tagRecyclerView)
        roomRecyclerView.layoutManager = LinearLayoutManager(this)

        // Step 2: Initialize the adapter
        val roomsAdapter = RoomAdapter()
        roomRecyclerView.adapter = roomsAdapter

        // Step 3: Load the data from the database and pass it to the adapter
        val tagDatabaseHelper = TagDatabaseHelper(this)
        val distinctRooms = tagDatabaseHelper.getDistinctRooms()

        // Step 4: Create a list of Room objects
        val roomsList = distinctRooms.map { roomNumber ->
            Room(roomNumber, tagDatabaseHelper.getTagCountByRoom(roomNumber))
        }

        // Step 5: Submit the list to the adapter
        roomsAdapter.submitList(roomsList)
    }
}
