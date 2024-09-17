//package com.example.aeroluggage.ui.screens
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.aeroluggage.R
//import com.example.aeroluggage.data.models.Room
//import com.example.aeroluggage.data.database.RoomAdapter
//import com.example.aeroluggage.data.database.TagDatabaseHelper
//
//class RoomHistoryScreen : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_room_history_screen)
//
////        supportActionBar?.apply {
////            title = "Room History"
////            setDisplayHomeAsUpEnabled(true)
////        }
////        supportActionBar!!.title = "Room History"
////        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//
//
//        // Step 1: Set up the RecyclerView
//        val roomRecyclerView: RecyclerView = findViewById(R.id.tagRecyclerView)
//        roomRecyclerView.layoutManager = LinearLayoutManager(this)
//
//        // Step 2: Initialize the adapter
//        val roomsAdapter = RoomAdapter()
//        roomRecyclerView.adapter = roomsAdapter
//
//        // Step 3: Load the data from the database and pass it to the adapter
//        val tagDatabaseHelper = TagDatabaseHelper(this)
//        val distinctRooms = tagDatabaseHelper.getDistinctRooms()
//
//        // Step 4: Create a list of Room objects
//        val roomsList = distinctRooms.map { roomNumber ->
//            Room(roomNumber, tagDatabaseHelper.getTagCountByRoom(roomNumber))
//        }
//
//        // Step 5: Submit the list to the adapter
//        roomsAdapter.submitList(roomsList)
//    }
//}

package com.example.aeroluggage.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aeroluggage.R
import com.example.aeroluggage.data.models.Room
import com.example.aeroluggage.data.database.RoomAdapter
import com.example.aeroluggage.data.database.TagDatabaseHelper

class RoomHistoryScreen : AppCompatActivity() {

    private lateinit var RoomAdapter: RoomAdapter
    private lateinit var TagDatabaseHelper: TagDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_history_screen)

        // Enable the back button in the action bar
        supportActionBar?.apply {
            title = "Room History"
            setDisplayHomeAsUpEnabled(true)
        }

        // Set up the RecyclerView
        val roomRecyclerView: RecyclerView = findViewById(R.id.tagRecyclerView)
        roomRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter
        val roomsAdapter = RoomAdapter()
        roomRecyclerView.adapter = roomsAdapter

        // Load the data from the database and pass it to the adapter
        val tagDatabaseHelper = TagDatabaseHelper(this)
        val distinctRooms = tagDatabaseHelper.getDistinctRooms()

        // Create a list of Room objects
        val roomsList = distinctRooms.map { roomNumber ->
            Room(roomNumber, tagDatabaseHelper.getTagCountByRoom(roomNumber))
        }

        // Submit the list to the adapter
        roomsAdapter.submitList(roomsList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate back to BarcodeScreen
                val intent = Intent(this, BarcodeScreen::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
