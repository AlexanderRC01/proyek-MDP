package com.myapplication

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminStatisticsActivity : AppCompatActivity() {

    private lateinit var tvTotalReports: TextView
    private lateinit var tvCompletedReports: TextView
    private lateinit var tvAverageRating: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_statistics)

        tvTotalReports = findViewById(R.id.tvTotalReports)
        tvCompletedReports = findViewById(R.id.tvCompletedReports)
        tvAverageRating = findViewById(R.id.tvAverageRating)

        loadStatistics()
    }

    private fun loadStatistics() {
        db.collection("reports").get()
            .addOnSuccessListener { snapshot ->
                val total = snapshot.size()
                var completed = 0
                var ratingSum = 0
                var ratingCount = 0

                for (doc in snapshot) {
                    val status = doc.getString("status")
                    if (status == "Selesai") {
                        completed++
                    }

                    val rating = doc.getLong("rating")?.toInt()
                    if (rating != null) {
                        ratingSum += rating
                        ratingCount++
                    }
                }

                val averageRating = if (ratingCount > 0) {
                    ratingSum.toFloat() / ratingCount
                } else {
                    0f
                }

                tvTotalReports.text = total.toString()
                tvCompletedReports.text = completed.toString()
                tvAverageRating.text = String.format("%.1f", averageRating)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat statistik", Toast.LENGTH_SHORT).show()
            }
    }
}
