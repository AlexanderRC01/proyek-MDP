package com.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class AddModeratorActivity : AppCompatActivity() {
    private lateinit var addModeratorButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ModeratorAdapter
    private val moderatorList = mutableListOf<User>()  // `User` bisa pakai data class khusus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_moderator)

        addModeratorButton = findViewById(R.id.addModeratorButton)
        recyclerView = findViewById(R.id.moderatorsRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ModeratorAdapter(moderatorList)
        recyclerView.adapter = adapter

        loadModerators()

        addModeratorButton.setOnClickListener {
            startActivity(Intent(this, AddModeratorActivity::class.java))
        }
    }

    private fun loadModerators() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("role", "moderator")
            .get()
            .addOnSuccessListener { snapshot ->
                moderatorList.clear()
                for (doc in snapshot) {
                    val user = doc.toObject(User::class.java).copy(uid = doc.id)
                    moderatorList.add(user)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat moderator", Toast.LENGTH_SHORT).show()
            }
    }
}