package com.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.myapplication.databinding.ActivityAdminHomeBinding
import com.google.firebase.firestore.Query


class AdminHome : AppCompatActivity() {
    private lateinit var binding: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadReportStats()
        loadRecentReports()

        binding.viewAllReportsButton.setOnClickListener {
            startActivity(Intent(this, AllReportsActivity::class.java))
        }

        binding.manageUsersButton.setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        binding.settingsButton.setOnClickListener {
            // buka halaman pengaturan atau logout
        }

    }

    private fun loadReportStats() {
        val db = FirebaseFirestore.getInstance()
        val reportsRef = db.collection("reports")

        // Total Reports
        reportsRef.get().addOnSuccessListener { snapshot ->
            binding.totalReportsCount.text = snapshot.size().toString()
        }

        // In Progress
        reportsRef.whereEqualTo("status", "in_progress")
            .get().addOnSuccessListener { snapshot ->
                binding.inProgressCount.text = snapshot.size().toString()
            }

        // Completed
        reportsRef.whereEqualTo("status", "completed")
            .get().addOnSuccessListener { snapshot ->
                binding.completedCount.text = snapshot.size().toString()
            }
    }

    private fun loadRecentReports() {
        val db = FirebaseFirestore.getInstance()
        val recentRef = db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)

        recentRef.get().addOnSuccessListener { result ->
            val reports = result.map { doc ->
                 doc.toObject(Report::class.java).copy(id = doc.id)
            }

            val adapter = ReportAdapter(reports) { report ->
                // Aksi saat laporan diklik, misalnya detail
            }

            binding.recentReportsRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.recentReportsRecyclerView.adapter = adapter
        }
    }

}