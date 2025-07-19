package com.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SettingAdmin : AppCompatActivity() {

    private lateinit var barChartDaily: BarChart
    private lateinit var pieChartCategory: PieChart
    private lateinit var pieChartStatus: PieChart
    private lateinit var spinnerUserModerator: Spinner
    private lateinit var btnFilterByDate: Button
    private lateinit var btnExportPdf: Button

    private val db = FirebaseFirestore.getInstance()
    private var allReports = listOf<Report>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_admin)

        barChartDaily = findViewById(R.id.barChartDaily)
        pieChartCategory = findViewById(R.id.pieChartCategory)
        pieChartStatus = findViewById(R.id.pieChartStatus)
        spinnerUserModerator = findViewById(R.id.spinnerUserModerator)
        btnFilterByDate = findViewById(R.id.btnFilterByDate)
        btnExportPdf = findViewById(R.id.btnExportPdf)

        val options = arrayOf("Semua", "User", "Moderator")
        spinnerUserModerator.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        loadReports()

        btnFilterByDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dialog = android.app.DatePickerDialog(this, { _, year, month, day ->
                val selected = Calendar.getInstance()
                selected.set(year, month, day)
                filterByDate(selected.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }

        btnExportPdf.setOnClickListener {
            exportToPdf(allReports)
        }

        spinnerUserModerator.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (options[position]) {
                    "User" -> updateCharts(allReports.filter { it.status != "approved" })
                    "Moderator" -> updateCharts(allReports.filter { it.status == "approved" || it.status == "selesai" })
                    else -> updateCharts(allReports)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadReports() {
        db.collection("reports").get().addOnSuccessListener { snapshot ->
            allReports = snapshot.toObjects(Report::class.java)
            updateCharts(allReports)
        }
    }

    private fun filterByDate(date: Date) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val filtered = allReports.filter {
            val ts = it.timestamp?.toDate()
            ts != null && sdf.format(ts) == sdf.format(date)
        }
        updateCharts(filtered)
    }

    private fun updateCharts(reports: List<Report>) {
        updateBarChart(reports)
        updateCategoryPieChart(reports)
        updateStatusPieChart(reports)
    }

    private fun updateBarChart(reports: List<Report>) {
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val grouped = reports.groupBy { dateFormat.format(it.timestamp?.toDate() ?: Date()) }
        val entries = grouped.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.size.toFloat())
        }
        val labels = grouped.keys.toList()
        val dataSet = BarDataSet(entries, "Jumlah laporan per hari").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
        }
        barChartDaily.data = BarData(dataSet)
        barChartDaily.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChartDaily.description = Description().apply { text = "" }
        barChartDaily.invalidate()
    }

    private fun updateCategoryPieChart(reports: List<Report>) {
        val grouped = reports.groupBy { it.category ?: "Lainnya" }
        val entries = grouped.entries.map {
            PieEntry(it.value.size.toFloat(), it.key)
        }
        val dataSet = PieDataSet(entries, "Kategori Terbanyak").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
        }
        pieChartCategory.data = PieData(dataSet)
        pieChartCategory.description = Description().apply { text = "" }
        pieChartCategory.invalidate()
    }

    private fun updateStatusPieChart(reports: List<Report>) {
        val grouped = reports.groupBy { it.status ?: "-" }
        val entries = grouped.entries.map {
            PieEntry(it.value.size.toFloat(), it.key)
        }
        val dataSet = PieDataSet(entries, "Tingkat Respon").apply {
            colors = ColorTemplate.PASTEL_COLORS.toList()
        }
        pieChartStatus.data = PieData(dataSet)
        pieChartStatus.description = Description().apply { text = "" }
        pieChartStatus.invalidate()
    }

    private fun exportToPdf(reports: List<Report>) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.textSize = 14f

        var y = 40f
        canvas.drawText("Laporan Statistik", 220f, y, paint)
        y += 30f

        val grouped = reports.groupBy { it.category ?: "-" }
        canvas.drawText("Kategori Laporan:", 20f, y, paint)
        y += 20f
        grouped.forEach {
            canvas.drawText("${it.key}: ${it.value.size}", 40f, y, paint)
            y += 20f
        }

        y += 20f
        val groupedStatus = reports.groupBy { it.status ?: "-" }
        canvas.drawText("Status Laporan:", 20f, y, paint)
        y += 20f
        groupedStatus.forEach {
            canvas.drawText("${it.key}: ${it.value.size}", 40f, y, paint)
            y += 20f
        }

        document.finishPage(page)

        // Simpan ke direktori aman: Android/data/com.myapplication/files
        val file = File(getExternalFilesDir(null), "laporan_statistik.pdf")
        try {
            document.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF disimpan di: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            openPdf(file) // buka file otomatis (opsional)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal menyimpan PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            document.close()
        }
    }


    private fun openPdf(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", file), "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak ada aplikasi pembaca PDF", Toast.LENGTH_SHORT).show()
        }
    }


}
