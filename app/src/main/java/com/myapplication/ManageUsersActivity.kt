package com.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ManageUsersActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        recyclerView = findViewById(R.id.usersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(
            userList,
            onChangeRoleClick = { user -> showChangeRoleDialog(user) },
            onDeleteClick = { user -> confirmDeleteUser(user) }
        )
        recyclerView.adapter = userAdapter

        loadUsers()
    }

    private fun loadUsers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                userList.clear()
                userList.addAll(snapshot.map { it.toObject(User::class.java).copy(uid = it.id) })
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data pengguna", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showChangeRoleDialog(user: User) {
        val roles = arrayOf("admin", "moderator", "warga")
        val currentIndex = roles.indexOf(user.role)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Ubah Role untuk ${user.name}")
            .setSingleChoiceItems(roles, currentIndex) { dialog, which ->
                val selectedRole = roles[which]
                FirebaseFirestore.getInstance().collection("users")
                    .document(user.uid ?: return@setSingleChoiceItems)
                    .update("role", selectedRole)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Role berhasil diubah ke $selectedRole", Toast.LENGTH_SHORT).show()
                        loadUsers() // Refresh list
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal mengubah role", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDeleteUser(user: User) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Hapus Pengguna")
            .setMessage("Yakin ingin menghapus akun ${user.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                FirebaseFirestore.getInstance().collection("users")
                    .document(user.uid ?: return@setPositiveButton)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pengguna berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadUsers() // Refresh list
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menghapus pengguna", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

}
