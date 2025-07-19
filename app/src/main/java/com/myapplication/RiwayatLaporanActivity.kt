package com.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RiwayatLaporanActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_laporan)

        recyclerView = findViewById(R.id.riwayatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadRiwayatLaporan()
    }

    private fun loadRiwayatLaporan() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("reports")
            .whereEqualTo("uid", uid) // ✅ hanya filter, tanpa order
            .get()
            .addOnSuccessListener { result ->
                val reports = result.map { doc ->
                    doc.toObject(Report::class.java).copy(id = doc.id)
                }

                reportAdapter = ReportAdapter(reports) { report ->
                    val intent = Intent(this, ReportDetailActivity::class.java)
                    intent.putExtra("REPORT_ID", report.id)
                    startActivity(intent)
                }

                recyclerView.adapter = reportAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
            }
    }

}