package com.example.aeroluggage

import RoomDataItem
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aeroluggage.databinding.ActivityBarcodeScreenBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.*

class BarcodeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityBarcodeScreenBinding
    private lateinit var db: TagDatabaseHelper
    private lateinit var tagAdapter: TagAdapter
    private lateinit var apiService: ApiService
    private var staffId: String? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the staff ID from the intent
        staffId = intent.getStringExtra("STAFF_ID")

        // Initialize Retrofit and ApiService with custom OkHttpClient
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ulmobservicestest.srilankan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getUnsafeOkHttpClient()) // Use custom OkHttpClient
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Initialize database and adapter
        db = TagDatabaseHelper(this)
        tagAdapter = TagAdapter(db.getAllTags(), this)
      //  tagAdapter = TagAdapter(db.getTagByIDTest(10), this)

        //handle Sync All button click
        binding.syncAllButton.setOnClickListener {
            tagAdapter.syncAllTags()
        }

        // Set up RecyclerView
        binding.tagRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tagRecyclerView.adapter = tagAdapter

        //save CheckId to the database
        binding.saveButton.setOnClickListener {
            val bagtag = binding.tagEditText.text.toString()
            val roomId = binding.roomEditText.tag?.toString() ?: "" // Retrieve CheckId from tag

            if (roomId.isNotEmpty() && bagtag.isNotEmpty()) {
                val dateTime = getCurrentDateTime()
                val tag = Tag(0, bagtag, roomId, dateTime, userID = staffId ?: "")
                db.insertTag(tag)
                tagAdapter.refreshData(db.getAllTags())
                binding.tagEditText.text.clear()
                binding.roomEditText.text.clear()
                Toast.makeText(this, "Bag Tag saved", Toast.LENGTH_SHORT).show()
                Log.d("BarcodeScreen", "Selected CheckId: $roomId")
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        // Find the roomButton in the layout
        val roomButton = findViewById<Button>(R.id.roomButton)

        // Set an OnClickListener to navigate to RoomHistoryScreen
        roomButton.setOnClickListener {
            val intent = Intent(this, RoomHistoryScreen::class.java)
            startActivity(intent)
        }


        // Fetch room data
        fetchRoomData()
    }

    //modifying fetchRoomData function to get both CheckId and CheckLabel
    private fun fetchRoomData() {
        apiService.getStorageRoomList().enqueue(object : Callback<List<RoomDataItem>> {
            override fun onResponse(
                call: Call<List<RoomDataItem>>,
                response: Response<List<RoomDataItem>>
            ) {
                if (response.isSuccessful) {
                    val roomData = response.body()
                    if (roomData != null) {
                        // Create a map of CheckLabel to CheckId
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

    //setting up the AutoCompleteTextView to display 'CheckLabel'
    private fun setupAutoCompleteTextView(roomMap: Map<String, String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roomMap.keys.toList())
        val roomEditText = findViewById<AutoCompleteTextView>(R.id.roomEditText)
        roomEditText.setAdapter(adapter)
        roomEditText.threshold = 1 // Start showing suggestions after 1 character

        // Handle the room selection to save CheckId instead of CheckLabel
        roomEditText.setOnItemClickListener { _, _, position, _ ->
            val selectedLabel = adapter.getItem(position)
            val selectedCheckId = roomMap[selectedLabel]
            roomEditText.tag = selectedCheckId  // Store the CheckId in the tag property
        }
    }

    // Get the current date and time as a formatted string
    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Trust all certificates (Unsafe)
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
