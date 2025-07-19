package com.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ModeratorReportAdapter(
    private val reportList: List<Report>,
    private val activity: Activity,
    private val imagePickerMap: MutableMap<Int, Uri?>,
    private val onPickImage: (Int) -> Unit
) : RecyclerView.Adapter<ModeratorReportAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.reportTitle)
        val description: TextView = itemView.findViewById(R.id.reportDescription)
        val image: ImageView = itemView.findViewById(R.id.reportImage)
        val status: TextView = itemView.findViewById(R.id.reportStatus)
        val btnVerify: Button = itemView.findViewById(R.id.btnVerify)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnMarkComplete: Button = itemView.findViewById(R.id.btnMarkComplete)
        val btnUploadProof: Button = itemView.findViewById(R.id.btnUploadProof)
        val proofImage: ImageView = itemView.findViewById(R.id.imageProof)
        val moderatorNote: EditText = itemView.findViewById(R.id.editTextModeratorNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_moderator, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reportList[position]
        val context = holder.itemView.context

        holder.title.text = report.title
        holder.description.text = report.description
        holder.status.text = "Status: ${report.status}"
        holder.status.visibility =
            if (report.status == "spam" || report.status == "duplicate") View.VISIBLE else View.GONE

        Glide.with(context).load(report.imageUrl).into(holder.image)

        // Tombol verifikasi
        holder.btnVerify.setOnClickListener {
            report.id?.let {
                updateStatus(it, "approved")
            } ?: Toast.makeText(context, "ID laporan tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        // Tombol tolak
        holder.btnReject.setOnClickListener {
            report.id?.let {
                updateStatus(it, "rejected")
            } ?: Toast.makeText(context, "ID laporan tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        // Tombol koreksi (belum diimplementasi navigasi khusus)
        holder.btnEdit.setOnClickListener {
            report.id?.let {
                val intent = Intent(context, ReportCorrectionActivity::class.java)
                intent.putExtra("REPORT_ID", it)
                context.startActivity(intent)
            } ?: Toast.makeText(context, "ID laporan tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        // Tombol tandai selesai
        holder.btnMarkComplete.setOnClickListener {
            report.id?.let {
                val catatan = holder.moderatorNote.text.toString()
                val proofUri = imagePickerMap[holder.adapterPosition]
                updateStatus(it, "selesai", catatan, proofUri)
            } ?: Toast.makeText(context, "ID laporan tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        // Tombol upload bukti
        holder.btnUploadProof.setOnClickListener {
            onPickImage(position)
        }

        // Menampilkan gambar bukti jika sudah dipilih
        val selectedUri = imagePickerMap[position]
        if (selectedUri != null) {
            holder.proofImage.setImageURI(selectedUri)
        } else {
            holder.proofImage.setImageDrawable(null)
        }
    }

    override fun getItemCount(): Int = reportList.size

    fun updateImageUri(position: Int, uri: Uri?) {
        imagePickerMap[position] = uri
        notifyItemChanged(position)
    }

    private fun updateStatus(
        reportId: String,
        status: String,
        note: String = "",
        proofUri: Uri? = null
    ) {
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf<String, Any>(
            "status" to status,
            "moderatorNote" to note
        )

        if (proofUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("proofs/$reportId.jpg")

            storageRef.putFile(proofUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        data["proofImageUrl"] = uri.toString()
                        db.collection("reports").document(reportId)
                            .update(data)
                            .addOnSuccessListener {
                                Toast.makeText(activity, "Status diperbarui", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(activity, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        } else {
            db.collection("reports").document(reportId)
                .update(data)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Status diperbarui", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
