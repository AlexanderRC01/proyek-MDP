package com.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModeratorAdapter(private val moderatorList: List<User>) :
    RecyclerView.Adapter<ModeratorAdapter.ModeratorViewHolder>() {

    class ModeratorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.moderatorName)
        val email: TextView = itemView.findViewById(R.id.moderatorEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeratorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_moderator, parent, false)
        return ModeratorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModeratorViewHolder, position: Int) {
        val user = moderatorList[position]
        holder.name.text = user.name
        holder.email.text = user.emailAddress
    }

    override fun getItemCount() = moderatorList.size
}

