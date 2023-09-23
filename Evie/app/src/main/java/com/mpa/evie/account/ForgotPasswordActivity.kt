package com.mpa.evie.account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mpa.evie.R

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var btnResetPassword: Button
    private lateinit var btnBack: FloatingActionButton
    private lateinit var etEmail: EditText
    private lateinit var auth: FirebaseAuth

    private companion object {
        private const val TAG = "ForgotPasswordActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        getSupportActionBar()?.hide()

        btnResetPassword = findViewById(R.id.btnResetPassword)
        btnBack = findViewById(R.id.btnBack)

        etEmail = findViewById(R.id.etEmail)
        auth = Firebase.auth

        btnBack.setOnClickListener {
            finish()
        }

        btnResetPassword.setOnClickListener {
            val email: String = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                etEmail.requestFocus()
                etEmail.setError("Email is required")
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.requestFocus()
                etEmail.setError("Please provide a valid email")
                return@setOnClickListener
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.w(TAG, "resetPassword:success")
                        Toast.makeText(
                            this,
                            "Check your email to reset your password",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Log.w(TAG, "resetPassword:failure", it.exception)
                        Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}