package com.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.Query
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.myapplication.databinding.ActivityLoginBinding
import com.myapplication.databinding.ActivityWargaHomeBinding

class WargaHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWargaHomeBinding
    private lateinit var reportAdapter: ReportAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentEmail = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWargaHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createReportButton.setOnClickListener {
            startActivity(Intent(this, AddReport::class.java))
        }

        binding.btnGoToProfile.setOnClickListener {
            startActivity(Intent(this, WargaProfileActivity::class.java))
        }

        binding.btnFilterMyReports.setOnClickListener {
            loadReports(showOnlyMine = true)
        }

        binding.btnFilterAllReports.setOnClickListener {
            loadReports(showOnlyMine = false)
        }

        setupRecyclerView()
        loadReports(showOnlyMine = false) // Default tampilkan semua laporan
    }

    private fun setupRecyclerView() {
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadReports(showOnlyMine: Boolean = false) {
        val query = if (showOnlyMine && currentEmail != null) {
            db.collection("reports").whereEqualTo("userName", currentEmail)
        } else {
            db.collection("reports")
        }

        query.orderBy("timestamp", Query.Direction.DESCENDING)
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

                binding.reportsRecyclerView.adapter = reportAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
            }
    }
}
