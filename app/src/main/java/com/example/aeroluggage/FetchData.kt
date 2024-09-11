package com.example.aeroluggage

data class FetchData(
    val ContentEncoding: Any,
    val ContentType: Any,
    val Data: List<Data>,
    val JsonRequestBehavior: Int,
    val MaxJsonLength: Any,
    val RecursionLimit: Any
)