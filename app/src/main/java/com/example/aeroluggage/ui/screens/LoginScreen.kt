package com.example.aeroluggage.ui.screens // Change to your actual package name

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aeroluggage.data.network.ApiService
import com.example.aeroluggage.R
import com.example.aeroluggage.data.database.TagDatabaseHelper
import com.example.aeroluggage.data.network.UnsafeOkHttpClient
import com.example.aeroluggage.data.models.UserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginScreen : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var databaseHelper: TagDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        val usernameEditText = findViewById<EditText>(R.id.editTextText2)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)

        databaseHelper = TagDatabaseHelper(this)

        // Initialize Retrofit with UnsafeOkHttpClient
        val httpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://ulmobservicestest.srilankan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        apiService = retrofit.create(ApiService::class.java)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {
                login(username, password)
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(username: String, password: String) {
        apiService.login(username, password).enqueue(object : Callback<UserModel> {
            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null && user.ReturnCode == "success") {
                        // Navigate to BarcodeScreen
                        val intent = Intent(this@LoginScreen, BarcodeScreen::class.java)
                        intent.putExtra("STAFF_ID", user.StaffNo) // Pass StaffNo to the next activity
                        intent.putExtra("STAFF_NAME", user.StaffName)  // Pass StaffName
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginScreen, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginScreen, "Failed to login", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                Toast.makeText(this@LoginScreen, "Connect to a stable connection", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
