package com.example.aeroluggage.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aeroluggage.R
import com.example.aeroluggage.TagAdapter
import com.example.aeroluggage.data.database.TagDatabaseHelper
import com.example.aeroluggage.data.models.Tag
import com.example.aeroluggage.data.network.ApiService
import com.example.aeroluggage.data.network.UnsafeOkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TagListActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var tagAdapter: TagAdapter
    private lateinit var tagDatabaseHelper: TagDatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_list)

//        // Set up the toolbar with a back button
//        val toolbar: Toolbar = findViewById(R.id.backtoolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        toolbar.setNavigationOnClickListener {
//            val intent = Intent(this, RoomHistoryScreen::class.java)
//            startActivity(intent)
//            finish()
//        }

        // Retrieve the room number passed from RoomsAdapter
        val roomNumber = intent.getStringExtra("ROOM_NUMBER")

        // Set the room number in a TextView (assuming you have one in the layout)
        val roomTextView: TextView = findViewById(R.id.roomTextView)
        roomTextView.text = "Tags in Room $roomNumber"

        // Use the room number to load tags from the database
        val tagDatabaseHelper = TagDatabaseHelper(this)
        val tags = tagDatabaseHelper.getTagsByRoom(roomNumber.toString())

        // Setup RecyclerView to display tags
        val tagRecyclerView: RecyclerView = findViewById(R.id.tagRecyclerView)
        tagRecyclerView.layoutManager = LinearLayoutManager(this)

        // Sort tags by dateTime in descending order
        val sortedTags = tags.sortedByDescending { it.dateTime }

        //setup the adapter and pass the tags in to it
        val tagAdapter = TagAdapter(sortedTags, this)
        tagRecyclerView.adapter = tagAdapter
    }




}

