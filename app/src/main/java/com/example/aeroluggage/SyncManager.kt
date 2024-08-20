////package com.example.aeroluggage
////
////class SyncManager {
////}
//
//package com.example.aeroluggage
//
//import android.util.Log
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class SyncManager(private val tagDatabaseHelper: TagDatabaseHelper) {
//
//    fun syncUnsyncedTags() {
//        val unsyncedTags = tagDatabaseHelper.getUnsyncedTags()
//
//        if (unsyncedTags.isNotEmpty()) {
//            val apiService = RetrofitClient.instance
//            val call = apiService.sendSyncData(unsyncedTags)
//
//            call.enqueue(object : Callback<SyncResponse> {
//                override fun onResponse(call: Call<SyncResponse>, response: Response<SyncResponse>) {
//                    if (response.isSuccessful) {
//                        val syncResponse = response.body()
//                        Log.d("SYNC_SUCCESS", "Tags synced successfully.")
//
//                        // Here, mark each synced tag as synced in the local database
//                        for (syncData in unsyncedTags) {
//                            tagDatabaseHelper.markAsSynced(syncData.BagTag)
//                        }
//
//                        Log.d("SYNC_INFO", "Server response: ${syncResponse?.ReturnCode} - ${syncResponse?.errorMessage}")
//                    } else {
//                        Log.e("SYNC_FAILURE", "Response was not successful: ${response.errorBody()?.string()}")
//                    }
//                }
//
//                override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
//                    Log.e("SYNC_ERROR", "Failed to sync tags: ${t.message}", t)
//                }
//            })
//        } else {
//            Log.d("SYNC_INFO", "No unsynced tags found.")
//        }
//    }
//}


package com.example.aeroluggage

import android.util.Log
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SyncManager(private val tagDatabaseHelper: TagDatabaseHelper) {

    fun syncSingleTag(syncData: SyncData) {
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

                    //Toast.makeText(tagDatabaseHelper.context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("SYNC_DEBUG", "Failed to sync data: ${response.code()}")
                    //Toast.makeText(tagDatabaseHelper.context, "Failed to sync data.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                Log.e("SYNC_DEBUG", "Sync failed: ${t.message}")
            // Toast.makeText(tagDatabaseHelper.context, "Sync failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
