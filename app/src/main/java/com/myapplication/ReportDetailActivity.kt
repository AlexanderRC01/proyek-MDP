package com.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import android.net.Uri
import android.widget.Button

class ReportDetailActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var titleView: TextView
    private lateinit var descView: TextView
    private lateinit var userView: TextView
    private lateinit var timestampView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        val reportId = intent.getStringExtra("REPORT_ID")

        imageView = findViewById(R.id.detailImage)
        titleView = findViewById(R.id.detailTitle)
        descView = findViewById(R.id.detailDescription)
        userView = findViewById(R.id.detailUser)
        timestampView = findViewById(R.id.detailTimestamp)

        if (reportId != null) {
            FirebaseFirestore.getInstance().collection("reports").document(reportId)
                .get()
                .addOnSuccessListener { doc ->
                    val report = doc.toObject(Report::class.java)
                    if (report != null) {
                        titleView.text = report.title
                        descView.text = report.description
                        userView.text = report.userName
                        timestampView.text = getRelativeTime(report.timestamp)

                        Glide.with(this)
                            .load(report.imageUrl)
                            .placeholder(R.drawable.download)
                            .into(imageView)

                        val btnViewOnMap = findViewById<Button>(R.id.btnViewOnMap)
                        btnViewOnMap.setOnClickListener {
                            val lat = report.latitude
                            val lon = report.longitude

                            if (lat != null && lon != null) {
                                val gmmIntentUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(Lokasi Laporan)")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                startActivity(mapIntent)
                            } else {
                                Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
                            }
                        }


                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memuat detail laporan", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getRelativeTime(timestamp: com.google.firebase.Timestamp?): String {
        val millis = timestamp?.toDate()?.time ?: 0L
        val now = System.currentTimeMillis()
        val diff = now - millis
        val minutes = diff / 60000
        return when {
            minutes < 60 -> "$minutes mins ago"
            minutes < 1440 -> "${minutes / 60} hours ago"
            else -> "${minutes / 1440} days ago"
        }
    }
}
