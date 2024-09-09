//package com.example.aeroluggage.ui.screens
//
//import com.example.aeroluggage.data.models.RoomDataItem
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.ArrayAdapter
//import android.widget.AutoCompleteTextView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.ActionBarDrawerToggle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.Toolbar
//import androidx.core.view.GravityCompat
//import androidx.drawerlayout.widget.DrawerLayout
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.aeroluggage.data.network.ApiService
//import com.example.aeroluggage.R
//import com.example.aeroluggage.ui.fragments.SettingsFragment
//import com.example.aeroluggage.data.models.Tag
//import com.example.aeroluggage.data.database.TagAdapter
//import com.example.aeroluggage.data.database.TagDatabaseHelper
//import com.example.aeroluggage.databinding.ActivityBarcodeScreenBinding
//import com.google.android.material.navigation.NavigationView
//import okhttp3.OkHttpClient
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.security.cert.X509Certificate
//import java.text.SimpleDateFormat
//import java.util.*
//import javax.net.ssl.*
//
//class BarcodeScreen : AppCompatActivity() {
//
//    private lateinit var binding: ActivityBarcodeScreenBinding
//    private lateinit var db: TagDatabaseHelper
//    private lateinit var tagAdapter: TagAdapter
//    private lateinit var apiService: ApiService
//    private var staffId: String? = null
////    private var staffName: String? = null
//    private lateinit var drawerLayout: DrawerLayout
//    private lateinit var toggle: ActionBarDrawerToggle
//
//    @SuppressLint("WrongViewCast")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityBarcodeScreenBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Initialize the toolbar and drawer
//        drawerLayout = findViewById(R.id.drawer_layout)
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//
//        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
//            R.string.open_nav,
//            R.string.close_nav
//        )
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//
//        // Retrieve Staff ID and Name from Intent
//        staffId = intent.getStringExtra("STAFF_ID")
//        val staffName = intent.getStringExtra("STAFF_NAME")
//
//        val navigationView: NavigationView = findViewById(R.id.nav_view)
//        //navigationView.setNavigationItemSelectedListener(this)
//        val headerView = navigationView.getHeaderView(0) // Index 0 for the first header view
//
//        // Find the TextViews in the header
//        val nameTextView = headerView.findViewById<TextView>(R.id.name)
//        val staffIdTextView = headerView.findViewById<TextView>(R.id.staff_id)
//
//        // Set the staffName and staffId
//        nameTextView.text = staffName
//        staffIdTextView.text = staffId
//
//        navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.nav_history -> {
//                    // Navigate to RoomHistory activity when the History option is clicked
//                    val intent = Intent(this, RoomHistoryScreen::class.java)
//                    startActivity(intent)
//                }
//
//                R.id.nav_settings -> supportFragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container, SettingsFragment()).commit()
//
//                R.id.logout ->  {
//                    // Clear session data if needed (like shared preferences or cached data)
//
//                    // Redirect to LoginScreen
//                    val intent = Intent(this, LoginScreen::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
//                    finish() // Ensure the BarcodeScreen activity is closed
//                }
//
//
//                    //Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
//            }
//            drawerLayout.closeDrawer(GravityCompat.START)
//            return@setNavigationItemSelectedListener true
//        }
//
//
//        // Initialize Retrofit and ApiService with custom OkHttpClient
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://ulmobservicestest.srilankan.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(getUnsafeOkHttpClient()) // Use custom OkHttpClient
//            .build()
//
//        apiService = retrofit.create(ApiService::class.java)
//
//        // Initialize database and adapter
//        db = TagDatabaseHelper(this)
//        tagAdapter = TagAdapter(db.getAllTags(), this)
//      //  tagAdapter = TagAdapter(db.getTagByIDTest(10), this)
//
//        //handle Sync All button click
//        binding.syncAllButton.setOnClickListener {
//            tagAdapter.syncAllTags()
//        }
//
//        // Set up RecyclerView
//        binding.tagRecyclerView.layoutManager = LinearLayoutManager(this)
//        binding.tagRecyclerView.adapter = tagAdapter
//
//        //save CheckId to the database
//        binding.saveButton.setOnClickListener {
//            val bagtag = binding.tagEditText.text.toString()
//            val roomId = binding.roomEditText.tag?.toString() ?: "" // Retrieve CheckId from tag
//
//            if (roomId.isNotEmpty() && bagtag.isNotEmpty()) {
//                val dateTime = getCurrentDateTime()
//                val tag = Tag(0, bagtag, roomId, dateTime, userID = staffId ?: "")
//                db.insertTag(tag)
//                tagAdapter.refreshData(db.getAllTags())
//                binding.tagEditText.text.clear()
//                binding.roomEditText.text.clear()
//                Toast.makeText(this, "Bag Tag saved", Toast.LENGTH_SHORT).show()
//                Log.d("BarcodeScreen", "Selected CheckId: $roomId")
//            } else {
//                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Fetch room data
//        fetchRoomData()
//    }
//
//    //modifying fetchRoomData function to get both CheckId and CheckLabel
//    private fun fetchRoomData() {
//        apiService.getStorageRoomList().enqueue(object : Callback<List<RoomDataItem>> {
//            override fun onResponse(
//                call: Call<List<RoomDataItem>>,
//                response: Response<List<RoomDataItem>>
//            ) {
//                if (response.isSuccessful) {
//                    val roomData = response.body()
//                    if (roomData != null) {
//                        // Create a map of CheckLabel to CheckId
//                        val roomMap = roomData.associate { it.CheckLabel to it.CheckId }
//                        setupAutoCompleteTextView(roomMap)
//                    } else {
//                        Log.e("BarcodeScreen", "Response body is null")
//                        Toast.makeText(this@BarcodeScreen, "No room labels found", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Log.e("BarcodeScreen", "Response error: ${response.errorBody()?.string()}")
//                    Toast.makeText(this@BarcodeScreen, "Error fetching room labels", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<List<RoomDataItem>>, t: Throwable) {
//                Log.e("BarcodeScreen", "API call failed: ${t.message}", t)
//                Toast.makeText(this@BarcodeScreen, "Failed to fetch room labels", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    //setting up the AutoCompleteTextView to display 'CheckLabel'
//    private fun setupAutoCompleteTextView(roomMap: Map<String, String>) {
//        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roomMap.keys.toList())
//        val roomEditText = findViewById<AutoCompleteTextView>(R.id.roomEditText)
//        roomEditText.setAdapter(adapter)
//        roomEditText.threshold = 1 // Start showing suggestions after 1 character
//
//        // Handle the room selection to save CheckId instead of CheckLabel
//        roomEditText.setOnItemClickListener { _, _, position, _ ->
//            val selectedLabel = adapter.getItem(position)
//            val selectedCheckId = roomMap[selectedLabel]
//            roomEditText.tag = selectedCheckId  // Store the CheckId in the tag property
//        }
//    }
//
//    // Get the current date and time as a formatted string
//    private fun getCurrentDateTime(): String {
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//        return dateFormat.format(Date())
//    }
//
//    // Trust all certificates (Unsafe)
//    private fun getUnsafeOkHttpClient(): OkHttpClient {
//        return try {
//            // Create a trust manager that does not validate certificate chains
//            val trustAllCerts = arrayOf<TrustManager>(
//                object : X509TrustManager {
//                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
//                    }
//
//                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
//                    }
//
//                    override fun getAcceptedIssuers(): Array<X509Certificate> {
//                        return arrayOf()
//                    }
//                }
//            )
//
//            // Install the all-trusting trust manager
//            val sslContext = SSLContext.getInstance("SSL")
//            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
//            val sslSocketFactory = sslContext.socketFactory
//
//            val builder = OkHttpClient.Builder()
//            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
//            builder.hostnameVerifier { _, _ -> true }
//            builder.build()
//        } catch (e: Exception) {
//            throw RuntimeException(e)
//        }
//    }
//}
//

package com.example.aeroluggage.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aeroluggage.data.network.ApiService
import com.example.aeroluggage.R
import com.example.aeroluggage.data.database.TagAdapter
import com.example.aeroluggage.data.database.TagDatabaseHelper
import com.example.aeroluggage.data.models.RoomDataItem
import com.example.aeroluggage.data.models.Tag
import com.example.aeroluggage.databinding.ActivityBarcodeScreenBinding
import com.example.aeroluggage.ui.fragments.SettingsFragment
import com.google.android.material.navigation.NavigationView
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class BarcodeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityBarcodeScreenBinding
    private lateinit var db: TagDatabaseHelper
    private lateinit var tagAdapter: TagAdapter
    private lateinit var apiService: ApiService
    private var staffId: String? = null
    private var isRoomFrozen = false

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the toolbar and drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Retrieve Staff ID and Name from Intent
        staffId = intent.getStringExtra("STAFF_ID")
        val staffName = intent.getStringExtra("STAFF_NAME")

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)

        // Find the TextViews in the header
        val nameTextView = headerView.findViewById<TextView>(R.id.name)
        val staffIdTextView = headerView.findViewById<TextView>(R.id.staff_id)

        // Set the staffName and staffId
        nameTextView.text = staffName
        staffIdTextView.text = staffId

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_history -> {
                    val intent = Intent(this, RoomHistoryScreen::class.java)
                    startActivity(intent)
                }
                R.id.nav_settings -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment()).commit()
                R.id.logout -> {
                    val intent = Intent(this, LoginScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Initialize Retrofit and ApiService
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ulmobservicestest.srilankan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getUnsafeOkHttpClient())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Initialize database and adapter
        db = TagDatabaseHelper(this)
        tagAdapter = TagAdapter(db.getAllTags(), this)

        // Set up RecyclerView
        binding.tagRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tagRecyclerView.adapter = tagAdapter

        // Handle Sync All button click
        binding.syncAllButton.setOnClickListener {
            tagAdapter.syncAllTags()
        }

        // Handle Save Button Click
        binding.saveButton.setOnClickListener {
            val bagTag = binding.tagEditText.text.toString()
            val roomId = binding.roomEditText.tag?.toString() ?: ""

            if (roomId.isNotEmpty() && bagTag.isNotEmpty()) {
                val dateTime = getCurrentDateTime()
                val tag = Tag(0, bagTag, roomId, dateTime, userID = staffId ?: "")
                db.insertTag(tag)
                tagAdapter.refreshData(db.getAllTags())
                binding.tagEditText.text.clear()

                if (!isRoomFrozen) {
                    freezeRoomNumber()
                }

                Toast.makeText(this, "Bag Tag saved", Toast.LENGTH_SHORT).show()
                Log.d("BarcodeScreen", "Selected CheckId: $roomId")
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Change Room Button Click
        binding.changeRoomButton.setOnClickListener {
            unfreezeRoomNumber()
        }

        // Fetch room data
        fetchRoomData()
    }

    // Freeze Room Number after it is entered
    private fun freezeRoomNumber() {
        binding.roomEditText.isEnabled = false
        binding.changeRoomButton.visibility = View.VISIBLE
        isRoomFrozen = true
    }

    // Unfreeze Room Number when user wants to change it
    private fun unfreezeRoomNumber() {
        binding.roomEditText.isEnabled = true
        binding.changeRoomButton.visibility = View.GONE
        binding.roomEditText.text.clear()
        isRoomFrozen = false
    }

    private fun fetchRoomData() {
        apiService.getStorageRoomList().enqueue(object : Callback<List<RoomDataItem>> {
            override fun onResponse(
                call: Call<List<RoomDataItem>>,
                response: Response<List<RoomDataItem>>
            ) {
                if (response.isSuccessful) {
                    val roomData = response.body()
                    if (roomData != null) {
                        val roomMap = roomData.associate { it.CheckLabel to it.CheckId }
                        setupAutoCompleteTextView(roomMap)
                    } else {
                        Log.e("BarcodeScreen", "Response body is null")
                        Toast.makeText(this@BarcodeScreen, "No room labels found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("BarcodeScreen", "Response error: ${response.errorBody()?.string()}")
                    Toast.makeText(this@BarcodeScreen, "Error fetching room labels", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RoomDataItem>>, t: Throwable) {
                Log.e("BarcodeScreen", "API call failed: ${t.message}", t)
                Toast.makeText(this@BarcodeScreen, "Failed to fetch room labels", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupAutoCompleteTextView(roomMap: Map<String, String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roomMap.keys.toList())
        val roomEditText = findViewById<AutoCompleteTextView>(R.id.roomEditText)
        roomEditText.setAdapter(adapter)
        roomEditText.threshold = 1

        roomEditText.setOnItemClickListener { _, _, position, _ ->
            val selectedLabel = adapter.getItem(position)
            val selectedCheckId = roomMap[selectedLabel]
            roomEditText.tag = selectedCheckId
        }
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

