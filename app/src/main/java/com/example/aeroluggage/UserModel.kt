package com.example.aeroluggage

data class UserModel(
    val StaffNo: String,
    val StaffName: String,
    val StaffPassword: String,
    val UserRole: String,
    val StaffImage: String?,
    val StaffTitle: String,
    val StaffEmail: String?,
    val StaffCategory: String?,
    val Designation: String?,
    val Station: String?,
    val ActiveStatus: String?,
    val CheckId: String?,
    val CheckLabel: String?,
    val AddedUser: String?,
    val AddedDate: String?,
    val AddedTime: String?,
    val LastUpdatedUser: String?,
    val LastUpdatedDate: String?,
    val EndDate: String?,
    val ValidPeriod: String?,
    val ReturnCode: String
)
