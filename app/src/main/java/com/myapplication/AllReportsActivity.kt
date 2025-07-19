package com.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.myapplication.databinding.ActivityAllReportsBinding

class AllReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllReportsBinding
    private lateinit var adapter: AdminReportAdapter
    private val reportList = mutableListOf<Report>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AdminReportAdapter(reportList,
            onViewClick = { report ->
                val intent = Intent(this, ReportDetailActivity::class.java)
                intent.putExtra("REPORT_ID", report.id)
                startActivity(intent)
            },
            onDeleteClick = { report ->
                showDeleteDialog(report)
            }
        )

        binding.recyclerViewAllReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAllReports.adapter = adapter

        loadAllReports()
    }

    private fun loadAllReports() {
        val db = FirebaseFirestore.getInstance()
        db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                reportList.clear()
                for (doc in snapshot) {
                    val report = doc.toObject(Report::class.java).copy(id = doc.id)
                    reportList.add(report)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteDialog(report: Report) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Laporan")
            .setMessage("Yakin ingin menghapus laporan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                FirebaseFirestore.getInstance().collection("reports")
                    .document(report.id ?: return@setPositiveButton)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Laporan dihapus", Toast.LENGTH_SHORT).show()
                        loadAllReports() // Refresh list
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menghapus laporan", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
