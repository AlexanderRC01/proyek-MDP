package com.myapplication

data class Report(
    val id: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "",
    val imageUrl: String = "",
    val profileUrl: String = "",
    val timestamp: com.google.firebase.Timestamp? = null)



