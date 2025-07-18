package com.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.myapplication.databinding.ActivityReportCorrectionBinding

class ReportCorrectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportCorrectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportCorrectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reportId = intent.getStringExtra("REPORT_ID")
        if (reportId == null) {
            Toast.makeText(this, "ID laporan tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // TODO: Tampilkan data laporan berdasarkan reportId dan izinkan koreksi

        binding.btnSaveCorrection.setOnClickListener {
            val correctionText = binding.editTextCorrectionNote.text.toString()

            if (correctionText.isNotBlank()) {
                // Simpan koreksi ke Firestore di laporan dengan ID ini
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("reports").document(reportId)
                    .update("moderator_note", correctionText, "status", "pending")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Koreksi disimpan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menyimpan koreksi", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Catatan koreksi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
