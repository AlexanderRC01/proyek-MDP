// ReportDetailActivity.kt
package com.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.myapplication.Report

class ReportDetailActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var titleView: TextView
    private lateinit var descView: TextView
    private lateinit var userView: TextView
    private lateinit var timestampView: TextView
    private lateinit var statusView: TextView
    private lateinit var ratingSection: LinearLayout
    private lateinit var ratingBar: RatingBar
    private lateinit var etFeedback: EditText
    private lateinit var btnSubmitRating: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        val reportId = intent.getStringExtra("REPORT_ID")

        imageView = findViewById(R.id.detailImage)
        titleView = findViewById(R.id.detailTitle)
        descView = findViewById(R.id.detailDescription)
        userView = findViewById(R.id.detailUser)
        timestampView = findViewById(R.id.detailTimestamp)
        statusView = findViewById(R.id.detailStatus)

        ratingSection = findViewById(R.id.ratingSection)
        ratingBar = findViewById(R.id.ratingBar)
        etFeedback = findViewById(R.id.etFeedback)
        btnSubmitRating = findViewById(R.id.btnSubmitRating)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            Toast.makeText(this, "Rating: $rating", Toast.LENGTH_SHORT).show()
        }

        ratingSection.visibility = View.GONE

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
                        statusView.text = "Status: ${report.status}"

                        Glide.with(this)
                            .load(report.imageUrl)
                            .placeholder(R.drawable.download)
                            .into(imageView)

                        val lat = report.latitude
                        val lon = report.longitude

                        findViewById<Button>(R.id.btnViewOnMap).setOnClickListener {
                            if (lat != null && lon != null) {
                                val gmmIntentUri =
                                    Uri.parse("geo:$lat,$lon?q=$lat,$lon(Lokasi Laporan)")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                startActivity(mapIntent)
                            } else {
                                Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        if (report.status == "selesai" && report.rating == null) {
                            ratingSection.visibility = View.VISIBLE

                            btnSubmitRating.setOnClickListener {
                                val rating = ratingBar.rating.toInt()
                                val feedback = etFeedback.text.toString()

                                FirebaseFirestore.getInstance().collection("reports")
                                    .document(reportId)
                                    .update(mapOf(
                                        "rating" to rating,
                                        "feedback" to feedback
                                    ))
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Terima kasih atas rating-nya!", Toast.LENGTH_SHORT).show()
                                        ratingSection.visibility = View.GONE
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Gagal menyimpan rating", Toast.LENGTH_SHORT).show()
                                    }
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
