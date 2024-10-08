package com.example.aeroluggage.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aeroluggage.data.network.ApiService
import com.example.aeroluggage.R
import com.example.aeroluggage.TagAdapter
import com.example.aeroluggage.data.database.TagDatabaseHelper
import com.example.aeroluggage.data.models.RoomDataItem
import com.example.aeroluggage.data.models.Tag
import com.example.aeroluggage.databinding.ActivityBarcodeScreenBinding
import com.example.aeroluggage.ui.fragments.InfoFragment
import com.example.aeroluggage.ui.fragments.SettingsFragment
import com.google.android.material.navigation.NavigationView
import okhttp3.OkHttpClient
import org.w3c.dom.Text
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

class BarcodeScreen : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityBarcodeScreenBinding
    private lateinit var db: TagDatabaseHelper
    private lateinit var tagAdapter: TagAdapter
    private lateinit var apiService: ApiService
    private var staffId: String? = null
    private var isRoomFrozen = false
    private lateinit var currentRoomId: String
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

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Retrieve Staff ID and Name from Intent
        staffId = intent.getStringExtra("STAFF_ID")
        val staffName = intent.getStringExtra("STAFF_NAME")

        //val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)

        // Find the TextViews in the header
        val nameTextView = headerView.findViewById<TextView>(R.id.name)
        val staffIdTextView = headerView.findViewById<TextView>(R.id.staff_id)

        // Set the staffName and staffId
        nameTextView.text = staffName
        staffIdTextView.text = staffId

        if (savedInstanceState == null){
            replaceFragment(SettingsFragment())
            navigationView.setCheckedItem(R.id.drawer_layout)
        }

        // Initialize Retrofit and ApiService for the
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ulmobservicestest.srilankan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getUnsafeOkHttpClient())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Initialize database and adapter
        db = TagDatabaseHelper(this)
        currentRoomId = ""  // Initialize with an empty string
        tagAdapter = TagAdapter(emptyList(), this)  // Start with an empty list

        // Set up RecyclerView
        binding.tagRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tagRecyclerView.adapter = tagAdapter

        // Hide textView, syncAllButton, and view01 initially
        binding.count.visibility = View.GONE
        binding.textView.visibility = View.GONE
        binding.syncAllButton.visibility = View.GONE
        binding.view01.visibility = View.GONE

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
                db.insertTag(this, tag){
                    loadTagsForRoom(roomId)
                    //updateTagCount(db.getTagsByRoom(roomId).size) // Update count after saving
                }
                loadTagsForRoom(roomId)
                binding.tagEditText.text.clear()

                if (!isRoomFrozen) {
                    freezeRoomNumber(roomId)
                }

                // Show textView, syncAllButton, and view01 after saving the tag
                binding.textView.visibility = View.VISIBLE
                binding.syncAllButton.visibility = View.VISIBLE
                binding.view01.visibility = View.VISIBLE
                binding.count.visibility = View.VISIBLE

                Toast.makeText(this, "Bag Tag saved", Toast.LENGTH_SHORT).show()
                Log.d("BarcodeScreen", "Selected CheckId: $roomId")


                // Hide the keyboard after saving
                hideKeyboard()
            } else {
                Toast.makeText(this, "Re-enter the room number & Tag", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Change Room Button Click
        binding.changeRoomButton.setOnClickListener {
            val newRoom = binding.roomEditText.text.toString()
            unfreezeRoomNumber()
            refreshTagsForRoom(newRoom)  // Refresh the RecyclerView for the new room with today's tags
            Toast.makeText(this, "Re-enter the Room Number", Toast.LENGTH_SHORT).show()

            //making the border visible when the room is changed
            binding.imageView18.visibility = View.VISIBLE

            //making textView, syncAllButton, and view01 invisible again
            binding.textView.visibility = View.INVISIBLE
            binding.syncAllButton.visibility = View.INVISIBLE
            binding.view01.visibility = View.INVISIBLE
            binding.count.visibility = View.INVISIBLE

        }

        // Fetch room data
        fetchRoomData()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_history -> {
                val intent = Intent(this, RoomHistoryScreen::class.java)
                startActivity(intent)
            }
            R.id.nav_info -> replaceFragment(InfoFragment())
            R.id.nav_settings -> replaceFragment(SettingsFragment())
            R.id.logout -> {
                logoutDialog()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    //for logout dialog
    private fun logoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                // User chose to exit, log out and finish activity
                val intent = Intent(this, LoginScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                //isExitDialogShowing = false // Reset the flag
            }
        // Show the dialog
        builder.show()

    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    // Freeze Room Number after it is entered
    private fun freezeRoomNumber(roomId: String) {
        binding.roomEditText.isEnabled = false
        binding.changeRoomButton.visibility = View.VISIBLE
        isRoomFrozen = true
        currentRoomId = roomId
    }

    // Unfreeze Room Number when user wants to change it
    private fun unfreezeRoomNumber() {
        binding.roomEditText.isEnabled = true
        binding.changeRoomButton.visibility = View.GONE
        binding.roomEditText.text.clear()
        isRoomFrozen = false
        currentRoomId = ""
        tagAdapter.refreshData(emptyList())  // Clear the RecyclerView when room is unfrozen
    }

    private fun loadTagsForRoom(roomId: String) {
        val tags = db.getTagsByRoom(roomId)
        val todayTags = filterTagsByDate(tags)
        val sortedTags = todayTags.sortedByDescending { it.dateTime }
        tagAdapter.refreshData(sortedTags)
        //updateTagCount(sortedTags.size) // Set the initial count
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
        val imageView18 = findViewById<ImageView>(R.id.imageView18)

        roomEditText.setAdapter(adapter)
        roomEditText.threshold = 1

        roomEditText.setOnItemClickListener { _, _, position, _ ->
            val selectedLabel = adapter.getItem(position)
            val selectedCheckId = roomMap[selectedLabel]
            roomEditText.tag = selectedCheckId

            // Hide imageView18 when a valid room number is selected
            imageView18.visibility = View.GONE

            //hide the keyboard after selecting the room
            hideKeyboard()
        }
    }

    private fun refreshTagsForRoom(roomId: String) {
        val tags = db.getTagsByRoom(roomId)
        val todayTags = filterTagsByDate(tags)  // Filter by today's date
        val sortedTags = todayTags.sortedByDescending { it.dateTime }
        tagAdapter.refreshData(sortedTags)
    }

    // Filters tags to include only those added today
    private fun filterTagsByDate(tags: List<Tag>): List<Tag> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return tags.filter { it.dateTime.startsWith(today) }
    }

    // Get the current date and time in the required format
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view != null) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    hideKeyboard()
                    view.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    // Function to hide the keyboard
    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private var isExitDialogShowing = false

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (!isExitDialogShowing) {
            showExitConfirmationDialog()
        } else {
            super.onBackPressed() // This will finish the activity if dialog is not shown
        }
    }

    private fun showExitConfirmationDialog() {
        if (!isFinishing) {
            isExitDialogShowing = true // Set the flag to true
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Exit Confirmation")
                .setMessage("Do you really want to exit the app?")
                .setPositiveButton("Yes") { dialog, _ ->
                    finishAffinity()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    isExitDialogShowing = false // Reset the flag
                }
                .setOnDismissListener {
                    isExitDialogShowing = false // Reset the flag when the dialog is dismissed
                }

            // Show the dialog
            builder.show()
        }
    }

}






