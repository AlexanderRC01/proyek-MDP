package com.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.myapplication.databinding.ActivityModeratorHomeBinding
import com.google.firebase.firestore.Query


class ModeratorHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModeratorHomeBinding
    private lateinit var reportAdapter: ReportAdapter
    private var reportList = mutableListOf<Report>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeratorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        reportAdapter = ReportAdapter(reportList) { report ->
            // TODO: Aksi klik jika perlu
        }
        binding.recyclerViewReports.adapter = reportAdapter

        loadReports("all") // default

        // Tombol filter
        binding.btnFilterAll.setOnClickListener { loadReports("all") }
        binding.btnFilterSpam.setOnClickListener { loadReports("spam") }
        binding.btnFilterDuplicate.setOnClickListener { loadReports("duplicate") }
    }

    private fun loadReports(filter: String) {
        val db = FirebaseFirestore.getInstance()
        var query: Query = db.collection("reports")

        when (filter) {
            "spam" -> query = query.whereEqualTo("status", "spam")
            "duplicate" -> query = query.whereEqualTo("status", "duplicate")
            // "all" tidak diberi filter
        }

        query.orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                reportList.clear()
                reportList.addAll(snapshot.map { it.toObject(Report::class.java).copy(id = it.id) })
                reportAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
            }
    }


}