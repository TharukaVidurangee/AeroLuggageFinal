package com.example.aeroluggage

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TagListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_list)

        // Retrieve the room number passed from RoomsAdapter
        val roomNumber = intent.getStringExtra("ROOM_NUMBER")

        // Set the room number in a TextView (assuming you have one in the layout)
        val roomTextView: TextView = findViewById(R.id.roomTextView)
        roomTextView.text = "Tags in $roomNumber"

        // Use the room number to load tags from the database
        val tagDatabaseHelper = TagDatabaseHelper(this)
        val tags = tagDatabaseHelper.getTagsByRoom(roomNumber.toString())

        // Setup RecyclerView to display tags
        val tagRecyclerView: RecyclerView = findViewById(R.id.tagRecyclerView)
        tagRecyclerView.layoutManager = LinearLayoutManager(this)

        //setup the adapter and pass the tags in to it
        val tagAdapter = TagAdapter(tags, this)
        tagRecyclerView.adapter = tagAdapter
    }
}

