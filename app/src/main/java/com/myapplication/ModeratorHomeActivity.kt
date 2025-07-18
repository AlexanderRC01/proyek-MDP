package com.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.myapplication.databinding.ActivityModeratorHomeBinding

class ModeratorHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModeratorHomeBinding
    private lateinit var reportAdapter: ModeratorReportAdapter
    private var reportList = mutableListOf<Report>()
    private val imagePickerMap = mutableMapOf<Int, Uri?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeratorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val onPickImage: (Int) -> Unit = { position ->
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pilih Bukti Gambar"), position)
        }

        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        reportAdapter = ModeratorReportAdapter(reportList, imagePickerMap, onPickImage)
        binding.recyclerViewReports.adapter = reportAdapter

        loadReports("all")

        binding.btnFilterAll.setOnClickListener { loadReports("all") }
        binding.btnFilterSpam.setOnClickListener { loadReports("spam") }
        binding.btnFilterDuplicate.setOnClickListener { loadReports("duplicate") }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadReports(filter: String) {
        val db = FirebaseFirestore.getInstance()
        var query: Query = db.collection("reports")

        when (filter) {
            "spam" -> query = query.whereEqualTo("status", "spam")
            "duplicate" -> query = query.whereEqualTo("status", "duplicate")
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
