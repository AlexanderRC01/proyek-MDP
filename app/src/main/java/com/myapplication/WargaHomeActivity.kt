package com.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        binding.btnGoToProfile.setOnClickListener {
            val intent = Intent(this, WargaProfileActivity::class.java)
            startActivity(intent)
        }


        loadReports()
    }


    private fun loadReports() {
        val db = FirebaseFirestore.getInstance()

        // ✅ Log testing
        db.collection("reports")
            .get()
            .addOnSuccessListener {
                Log.d("Firestore", "Berhasil ambil laporan: ${it.size()}")
            }
            .addOnFailureListener {
                Log.e("Firestore", "Gagal ambil data", it)
            }
        Log.d("ReportLoad", "1")

        Log.d("ReportLoad", "2")

        db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val reports = result.map { doc ->
                    doc.toObject(Report::class.java).copy(id = doc.id)
                }

                reportAdapter = ReportAdapter(reports) { report ->
                    val intent = Intent(this, ReportDetailActivity::class.java)
                    intent.putExtra("REPORT_ID", report.id) // ← ID dikirim di sini
                    startActivity(intent)
                }


                Toast.makeText(this, "Berhasil memuat laporan", Toast.LENGTH_SHORT).show()
                Log.d("ReportLoad", "3")

                Log.d("ReportLoad", "Reports fetched: ${reports.size}")
                Log.d("ReportLoad", "4")

                recyclerView.adapter = reportAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
            }
    }
}
