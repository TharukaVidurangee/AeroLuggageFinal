package com.example.aeroluggage.sync

interface SyncCallback {
    fun onSyncSuccess()
    fun onSyncFailure()
}