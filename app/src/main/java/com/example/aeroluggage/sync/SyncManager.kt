package com.example.aeroluggage.sync

import android.content.Context
import android.util.Log
import com.example.aeroluggage.data.database.TagDatabaseHelper
import com.example.aeroluggage.data.models.SyncData
import com.example.aeroluggage.data.network.RetrofitClient
import com.example.aeroluggage.data.network.SyncResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SyncManager(
    private val context: Context,
    private val tagDatabaseHelper: TagDatabaseHelper
) {

    fun syncSingleTag(syncData: SyncData, callback: SyncCallback) {
        val apiService = RetrofitClient.instance
        val call = apiService.sendSyncData(syncData)  // Send only the specific tag

        call.enqueue(object : Callback<SyncResponse> {
            override fun onResponse(call: Call<SyncResponse>, response: Response<SyncResponse>) {
                if (response.isSuccessful) {
                    val syncResponse = response.body()
                    val message = syncResponse?.ReturnCode ?: "Sync successful"
                    Log.d("SYNC_DEBUG", "Server message: $message")

                    // Mark the specific tag as synced in the local database
                    tagDatabaseHelper.markAsSynced(syncData.BagTag)

                    //to notify success
                    callback.onSyncSuccess()

                    //Toast.makeText(tagDatabaseHelper.context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("SYNC_DEBUG", "Failed to sync data: ${response.code()}")
                    //Toast.makeText(tagDatabaseHelper.context, "Failed to sync data.", Toast.LENGTH_SHORT).show()

                    //to notify failure
                    callback.onSyncFailure()
                }
            }
            override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                Log.e("SYNC_DEBUG", "Sync failed: ${t.message}")
                //Toast.makeText(tagDatabaseHelper.context, "Sync failed: ${t.message}", Toast.LENGTH_SHORT).show()
                //to notify failure
                callback.onSyncFailure()
            }
        })
    }
}
