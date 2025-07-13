package com.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class WargaProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var etAge: EditText
    private lateinit var btnSave: Button
    private lateinit var btnChangePhoto: TextView
    private lateinit var imgPhoto: ImageView

    private var selectedPhotoUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warga_profile)

        etName = findViewById(R.id.etUserName)
        etEmail = findViewById(R.id.etUserEmail)
        etPhone = findViewById(R.id.etUserPhone)
        etAddress = findViewById(R.id.etUserAddress)
        etAge = findViewById(R.id.etUserAge)
        btnSave = findViewById(R.id.btnSaveProfile)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        imgPhoto = findViewById(R.id.imgUserPhoto)

        loadUserData()

        btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnSave.setOnClickListener {
            saveUserData()
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                etName.setText(doc.getString("name") ?: "")
                etEmail.setText(doc.getString("emailAddress") ?: "")
                etPhone.setText(doc.getString("phone") ?: "")
                etAddress.setText(doc.getString("address") ?: "")
                etAge.setText(doc.getLong("age")?.toString() ?: "")

                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(photoUrl).into(imgPhoto)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return
        val userMap = mapOf(
            "name" to etName.text.toString(),
            "emailAddress" to etEmail.text.toString(),
            "phone" to etPhone.text.toString(),
            "address" to etAddress.text.toString(),
            "age" to etAge.text.toString().toIntOrNull()
        )

        db.collection("users").document(uid).update(userMap)
            .addOnSuccessListener {
                if (selectedPhotoUri != null) {
                    uploadPhoto(uid)
                } else {
                    Toast.makeText(this, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPhoto(uid: String) {
        val ref = storage.reference.child("profile_photos/$uid.jpg")
        ref.putFile(selectedPhotoUri!!)
            .continueWithTask { task -> ref.downloadUrl }
            .addOnSuccessListener { uri ->
                db.collection("users").document(uid)
                    .update("photoUrl", uri.toString())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal upload foto", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            imgPhoto.setImageURI(selectedPhotoUri)
        }
    }
}
