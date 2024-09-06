package com.example.aeroluggage.data.network

import com.example.aeroluggage.domain.storage.StorageRoomDetails

data class SyncResponse(
    val TransId: Int,
    val StorageRoom: StorageRoomDetails,
    val BagTag: String,
    val SyncDate: String?,
    val CheckId: String?,
    val CheckLabel: String?,
    val AddedUser: String,
    val AddedDate: String,
    val AddedTime: String?,
    val LastUpdatedUser: String?,
    val LastUpdatedDate: String?,
    val EndDate: String?,
    val ValidPeriod: String?,
    val errorMessage: String?,
    val ReturnCode: String
)
