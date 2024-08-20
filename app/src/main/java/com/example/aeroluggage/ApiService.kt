package com.example.aeroluggage // Change to your actual package name

import RoomDataItem
import android.content.SyncRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("Easypass_revamp/Login/GetLoginValidationForStoragRoom")
    fun login(
        @Field("StaffNo") staffNo: String,
        @Field("StaffPassword") staffPassword: String
    ): Call<UserModel>

    @GET("Easypass_revamp/Home/GetStorageRoomList") // Ensure this is the correct endpoint
    fun getStorageRoomList(): Call<List<RoomDataItem>>

    @POST("Easypass_revamp/StorageRoom/SaveStorageRoomBag")
    //fun sendSyncData(@Body syncDataList: List<SyncData>): Call<SyncResponse>
    fun sendSyncData(@Body request: SyncData): Call<SyncResponse>
}