package com.bangkit.pedulibumil.auth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bangkit.pedulibumil.MainActivity
import com.bangkit.pedulibumil.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IdentityActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var tvDateOfBirth: TextView
    private lateinit var etKandungan: EditText
    private lateinit var btnSubmit: Button

    private val viewModel: IdentityViewModel by viewModels()
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity)

        // Inisialisasi komponen UI
        etName = findViewById(R.id.etName)
        tvDateOfBirth = findViewById(R.id.etDateOfBirth)
        etKandungan = findViewById(R.id.etKandungan)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Pantau apakah data sudah dikirimkan
        viewModel.isDataSubmitted.observe(this, Observer { isSubmitted ->
            if (isSubmitted) {
                navigateToMainActivity()
            }
        })

        // Atur ulang data jika sudah disimpan di ViewModel
        viewModel.name.observe(this) { name ->
            if (name != null) etName.setText(name)
        }

        viewModel.dateOfBirth.observe(this) { dateOfBirth ->
            if (dateOfBirth != null) tvDateOfBirth.text = dateOfBirth
        }

        viewModel.pregnancyAge.observe(this) { pregnancyAge ->
            if (pregnancyAge != null) etKandungan.setText(pregnancyAge)
        }

        // Tampilkan DatePicker saat tanggal lahir diklik
        tvDateOfBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val selectedDate = "$year-${month + 1}-$dayOfMonth"
                tvDateOfBirth.text = selectedDate
                viewModel.setDateOfBirth(selectedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSubmit.setOnClickListener {
            val sName = etName.text.toString().trim()
            val sDateOfBirth = tvDateOfBirth.text.toString().trim()
            val sKandungan = etKandungan.text.toString().trim()

            if (sName.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (sDateOfBirth.isEmpty()) {
                Toast.makeText(this, "Tanggal lahir tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (sKandungan.isEmpty()) {
                Toast.makeText(this, "Usia kandungan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.setName(sName)
            viewModel.setDateOfBirth(sDateOfBirth)
            viewModel.setPregnancyAge(sKandungan)

            val age = calculateAge(sDateOfBirth)

            val userMap = hashMapOf(
                "nama" to sName,
                "tanggal_lahir" to sDateOfBirth,
                "umur" to age,
                "usiakandungan" to sKandungan
            )

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("user").document(userId).set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                    viewModel.markDataAsSubmitted()
                }
                .addOnFailureListener { e ->
                    Log.e("Identity", "Error saving data", e)
                    Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun calculateAge(dateOfBirth: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDate = dateFormat.parse(dateOfBirth) ?: return 0
        val today = Calendar.getInstance()
        val birthCalendar = Calendar.getInstance().apply { time = birthDate }

        var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age -= 1
        }
        return age
    }
}
