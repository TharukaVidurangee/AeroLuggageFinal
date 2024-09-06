package com.example.aeroluggage.data.models

import com.example.aeroluggage.domain.storage.StorageRoom

data class SyncData(
    val AddedDate: String,
    val AddedUser: String,
    val BagTag: String,
    val StorageRoom: StorageRoom
)
