package com.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.Query
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.myapplication.databinding.ActivityLoginBinding
import com.myapplication.databinding.ActivityWargaHomeBinding

class WargaHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWargaHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var addReportButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi ViewBinding
        binding = ActivityWargaHomeBinding.inflate(layoutInflater)
        setContentView(binding.root) // <-- WAJIB supaya layout tampil

        // RecyclerView sudah terhubung di XML via binding
        recyclerView = binding.reportsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Kalau kamu punya tombol tambah laporan, ambil juga via binding
        addReportButton = binding.createReportButton
        addReportButton.setOnClickListener {
            startActivity(Intent(this, AddReport::class.java))
        }

        loadReports()
    }


    private fun loadReports() {
        val db = FirebaseFirestore.getInstance()
        db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val reports = result.map { doc ->
                    val report = doc.toObject(Report::class.java)
                    report.copy(id = doc.id)
                }

                reportAdapter = ReportAdapter(reports) { report ->
                    val intent = Intent(this, ReportDetailActivity::class.java)
                    intent.putExtra("REPORT_ID", report.id)
                    startActivity(intent)
                }

                Toast.makeText(this, "Berhasil memuat laporan", Toast.LENGTH_SHORT).show()

                recyclerView.adapter = reportAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
            }
    }
}
