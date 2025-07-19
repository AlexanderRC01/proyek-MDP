package com.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.maps.model.LatLng // ✅ BENAR
import java.util.UUID

class AddReport : AppCompatActivity() {
    private lateinit var titleInput: EditText
    private lateinit var descInput: EditText
    private lateinit var btnUploadPhoto: Button
    private lateinit var btnSubmit: Button
    private lateinit var imgUri: Uri

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 101

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatLng: LatLng? = null

    private lateinit var categorySpinner: Spinner
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_report)

        categorySpinner = findViewById(R.id.spinnerCategory)

        val categories = listOf("Jalan Rusak", "Lampu Mati", "Banjir", "Sampah", "Lainnya")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
            }
        })

        checkStoragePermission()
        checkLocationPermission()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        titleInput = findViewById(R.id.editTextReportTitle)
        descInput = findViewById(R.id.editTextDescription)
        btnUploadPhoto = findViewById(R.id.buttonUploadPhoto)
        btnSubmit = findViewById(R.id.buttonSubmitReport)

        btnUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT) // ← perbaikan penting
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        findViewById<Button>(R.id.buttonSelectLocation).setOnClickListener {
            fetchCurrentLocation()
        }

        btnSubmit.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageAndSaveReport() // upload dulu, lalu simpan
            } else {
                // langsung simpan laporan tanpa gambar
                saveReportToFirestore("https://via.placeholder.com/600x400") // atau kosong: ""
            }
        }

    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    102
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if ((requestCode == 101 || requestCode == 102) &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Izin diberikan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Izin ditolak", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.e("AddReport", "Failed to persist URI permission: ${e.message}")
                }
                selectedImageUri = uri
                findViewById<ImageView>(R.id.imagePreview).setImageURI(uri)
                Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show()
                Log.d("AddReport", "Image URI: $uri")
            } else {
                Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun uploadImageAndSaveReport() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().reference
            .child("reportImages/${System.currentTimeMillis()}_$fileName.jpg")

        Log.d("UploadDebug", "Uploading image to: ${storageRef.path}")
        Log.d("UploadDebug", "Image URI: $selectedImageUri")

        storageRef.putFile(selectedImageUri!!)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Upload gagal")
                }
                storageRef.downloadUrl
            }
            .addOnSuccessListener { uri ->
                Log.d("UploadDebug", "Image uploaded to: $uri")
                saveReportToFirestore(uri.toString())
            }
            .addOnFailureListener {
                Log.e("UploadDebug", "Upload error: ${it.message}")
                Toast.makeText(this, "Upload gambar gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveReportToFirestore(imageUrl: String) {
        val title = titleInput.text.toString().trim()
        val desc = descInput.text.toString().trim()

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Judul dan deskripsi harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Silakan pilih kategori", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        val userName = user?.email ?: "Anonymous"
        val uid = user?.uid ?: "unknown"

        val report = hashMapOf(
            "title" to title,
            "description" to desc,
            "imageUrl" to imageUrl,
            "profileUrl" to "https://via.placeholder.com/100x100",
            "userName" to userName,
            "uid" to uid,
            "status" to "pending", // kamu bisa pakai default ini atau 'approved' langsung
            "timestamp" to FieldValue.serverTimestamp(),
            "latitude" to currentLatLng?.latitude,
            "longitude" to currentLatLng?.longitude,
            "category" to selectedCategory
        )

        FirebaseFirestore.getInstance().collection("reports").add(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan laporan", Toast.LENGTH_SHORT).show()
            }
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                103
            )
        }
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Izin lokasi belum diberikan", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLatLng = LatLng(location.latitude, location.longitude)
                    Toast.makeText(this, "Lokasi ditemukan: ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
                }
            }
    }
}