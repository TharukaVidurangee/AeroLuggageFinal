package com.example.aeroluggage.data.network // Change to your actual package name

import com.example.aeroluggage.ApiResponse
import com.example.aeroluggage.data.models.RoomDataItem
import com.example.aeroluggage.data.models.SyncData
import com.example.aeroluggage.data.models.Tag
import com.example.aeroluggage.data.models.UserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("Easypass_revamp/Login/GetLoginValidationForStoragRoom")
    fun login(
        @Field("StaffNo") staffNo: String,
        @Field("StaffPassword") staffPassword: String
    ): Call<UserModel>

    @GET("Easypass_revamp/Home/GetStorageRoomList")
    fun getStorageRoomList(): Call<List<RoomDataItem>>

    @POST("Easypass_revamp/StorageRoom/SaveStorageRoomBag")
    fun sendSyncData(@Body request: SyncData): Call<SyncResponse>

    @GET("Easypass_revamp/StorageRoom/GetStorageRoomBagListByUser")
    fun getTagsByUser(@Query("StaffNo") staffNo: String): Call<ApiResponse>
}