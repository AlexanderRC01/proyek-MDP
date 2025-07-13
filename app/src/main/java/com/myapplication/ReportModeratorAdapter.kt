package com.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp

class ReportModeratorAdapter(
    private val reportList: List<Report>,
    private val onVerifyClick: (Report) -> Unit,
    private val onRejectClick: (Report) -> Unit,
    private val onEditClick: (Report) -> Unit,
    private val onMarkCompleteClick: (Report) -> Unit,
    private val onUploadProofClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportModeratorAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reportTitle: TextView = itemView.findViewById(R.id.reportTitle)
        val reportImage: ImageView = itemView.findViewById(R.id.reportImage)
        val reportDescription: TextView = itemView.findViewById(R.id.reportDescription)
        val reportStatus: TextView = itemView.findViewById(R.id.reportStatus)

        val btnVerify: Button = itemView.findViewById(R.id.btnVerify)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnMarkComplete: Button = itemView.findViewById(R.id.btnMarkComplete)
        val btnUploadProof: Button = itemView.findViewById(R.id.btnUploadProof)

        val imageProof: ImageView = itemView.findViewById(R.id.imageProof)
        val editTextModeratorNote: EditText = itemView.findViewById(R.id.editTextModeratorNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_moderator, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        holder.reportTitle.text = report.title
        holder.reportDescription.text = report.description

        Glide.with(holder.reportImage.context)
            .load(report.imageUrl)
            .placeholder(R.drawable.download)
            .into(holder.reportImage)

        // Set status jika ada
        if (!report.status.isNullOrEmpty()) {
            holder.reportStatus.visibility = View.VISIBLE
            holder.reportStatus.text = "Status: ${report.status}"
        } else {
            holder.reportStatus.visibility = View.GONE
        }

        // Tombol aksi
        holder.btnVerify.setOnClickListener { onVerifyClick(report) }
        holder.btnReject.setOnClickListener { onRejectClick(report) }
        holder.btnEdit.setOnClickListener { onEditClick(report) }
        holder.btnMarkComplete.setOnClickListener { onMarkCompleteClick(report) }
        holder.btnUploadProof.setOnClickListener { onUploadProofClick(report) }

    }

    override fun getItemCount() = reportList.size
}
