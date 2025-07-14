package com.myapplication

data class Report(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val profileUrl: String = "",
    val userName: String = "",
    val status: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)



