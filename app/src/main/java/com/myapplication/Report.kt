package com.myapplication

data class Report(
    val id: String = "",
    val userName: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val profileUrl: String = "",
    val timestamp: com.google.firebase.Timestamp? = null)

