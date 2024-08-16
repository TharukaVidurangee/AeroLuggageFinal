package com.example.aeroluggage

import android.util.Log
import com.google.gson.Gson

object JsonUtils {

    fun convertToJSON(syncDataList: List<SyncData>): String {
        val gson = Gson()
        return gson.toJson(syncDataList)
        Log.d("JSON_DEBUG", "Converted to JSON: $gson.toJson(syncDataList)")
    }
}

//    fun convertToJSON(syncDataList: List<SyncData>): String {
//        val gson = Gson()
//        val json = gson.toJson(syncDataList)
//        Log.d("JSON_DEBUG", "Converted to JSON: $json")
//        return json
//    }
