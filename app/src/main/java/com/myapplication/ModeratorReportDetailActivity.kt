package com.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class ModeratorReportDetailActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnUpdateStatus: Button

    private var reportId: String? = null
    private var currentStatus: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moderator_report_detail)

        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus)

        // Set isi Spinner
        val statusList = listOf("pending", "approved", "selesai")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter

        reportId = intent.getStringExtra("REPORT_ID")
        if (reportId == null) {
            Toast.makeText(this, "ID laporan tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadReportDetail()

        btnUpdateStatus.setOnClickListener {
            val newStatus = spinnerStatus.selectedItem.toString()
            updateReportStatus(newStatus)
        }
    }

    private fun loadReportDetail() {
        val db = FirebaseFirestore.getInstance()
        db.collection("reports").document(reportId!!)
            .get()
            .addOnSuccessListener { doc ->
                val report = doc.toObject(Report::class.java)
                if (report != null) {
                    tvTitle.text = report.title
                    tvDescription.text = report.description
                    currentStatus = report.status

                    // Pilih status saat ini di Spinner
                    val statusIndex = (spinnerStatus.adapter as ArrayAdapter<String>).getPosition(currentStatus)
                    spinnerStatus.setSelection(statusIndex)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil detail laporan", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateReportStatus(newStatus: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("reports").document(reportId!!)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Status berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke daftar laporan
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
            }
    }
}