package com.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp

class AdminReportAdapter(
    private val reports: List<Report>,
    private val onViewClick: (Report) -> Unit,
    private val onDeleteClick: (Report) -> Unit
) : RecyclerView.Adapter<AdminReportAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.reportTitle)
        val description: TextView = itemView.findViewById(R.id.reportDescription)
        val user: TextView = itemView.findViewById(R.id.reportUser)
        val timestamp: TextView = itemView.findViewById(R.id.reportTimestamp)
        val btnView: Button = itemView.findViewById(R.id.btnViewDetail)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_admin, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = reports.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]

        holder.title.text = report.title
        holder.description.text = report.description
        holder.user.text = "Dikirim oleh: ${report.userName}"
        holder.timestamp.text = getRelativeTime(report.timestamp)

        holder.btnView.setOnClickListener { onViewClick(report) }
        holder.btnDelete.setOnClickListener { onDeleteClick(report) }
    }

    private fun getRelativeTime(timestamp: Timestamp?): String {
        val millis = timestamp?.toDate()?.time ?: 0L
        val now = System.currentTimeMillis()
        val diff = now - millis
        val minutes = diff / 60000
        return when {
            minutes < 60 -> "$minutes menit lalu"
            minutes < 1440 -> "${minutes / 60} jam lalu"
            else -> "${minutes / 1440} hari lalu"
        }
    }
}
