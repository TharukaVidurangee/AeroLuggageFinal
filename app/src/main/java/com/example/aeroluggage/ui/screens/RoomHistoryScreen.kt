package com.example.aeroluggage.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toolbar
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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_history_screen)

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
        }.sortedBy { it.roomNumber }

        // Set up the back button to navigate back to BarcodeScreen
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, BarcodeScreen::class.java)
            startActivity(intent)
            finish()
        }

        // Submit the list to the adapter
        roomsAdapter.submitList(roomsList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate back to BarcodeScreen
                val intent = Intent(this, BarcodeScreen::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
