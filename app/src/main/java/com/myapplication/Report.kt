package com.myapplication

import com.google.firebase.Timestamp

data class Report(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val userName: String? = null,
    val profileUrl: String? = null,
    val timestamp: Timestamp? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String? = null,
    val rating: Int? = null // ⭐ Tambahkan ini
)







