package com.mpa.evie.info

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.mpa.evie.R

class NameActivity : AppCompatActivity() {

    private lateinit var btnConfirm: Button
    private lateinit var etFullName: EditText

    private val auth = Firebase.auth

    private companion object {
        private const val TAG = "NameActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name)

        btnConfirm = findViewById(R.id.btnConfirm)
        etFullName = findViewById(R.id.etFullName)

        btnConfirm.setOnClickListener {
            val fullName: String = etFullName.text.toString()
            if (fullName.isEmpty()) {
                etFullName.requestFocus()
                etFullName.setError("Name is required")
            }
            val profileUpdates = userProfileChangeRequest {
                displayName = fullName
            }

            auth.currentUser!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "displayName updated")
                        finish()
                    } else {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}