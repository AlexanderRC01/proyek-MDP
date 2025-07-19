package com.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ReportAdapter(
    private val reportList: List<Report>,
    private val onViewClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgReport: ImageView = itemView.findViewById(R.id.imgReport)
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val btnView: Button = itemView.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        holder.tvUserName.text = report.userName
        holder.tvDescription.text = report.description
        holder.tvTimestamp.text = getRelativeTime(report.timestamp)

        Glide.with(holder.imgReport.context)
            .load(report.imageUrl)
            .placeholder(R.drawable.download)
            .into(holder.imgReport)

        Glide.with(holder.imgProfile.context)
            .load(report.profileUrl)
            .placeholder(R.drawable.bgcsplash)
            .into(holder.imgProfile)

        holder.btnView.setOnClickListener {
            onViewClick(report)
        }
    }

    override fun getItemCount() = reportList.size

    private fun getRelativeTime(timestamp: com.google.firebase.Timestamp?): String {
        val millis = timestamp?.toDate()?.time ?: 0L
        val now = System.currentTimeMillis()
        val diff = now - millis
        val minutes = diff / 60000

        return when {
            minutes < 60 -> "$minutes mins ago"
            minutes < 1440 -> "${minutes / 60} hours ago"
            else -> "${minutes / 1440} days ago"
        }
    }
}
