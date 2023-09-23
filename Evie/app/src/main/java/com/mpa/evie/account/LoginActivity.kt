package com.mpa.evie.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mpa.evie.HomeActivity
import com.mpa.evie.R


class LoginActivity : AppCompatActivity() {

    private lateinit var btnSignIn: Button
    private lateinit var btnBack: FloatingActionButton
    private lateinit var btnForgotPassword: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var auth: FirebaseAuth

    private companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        getSupportActionBar()?.hide()

        btnSignIn = findViewById(R.id.btnSignIn)
        btnBack = findViewById(R.id.btnBack)
        btnForgotPassword = findViewById(R.id.btnForgotPassword)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        auth = Firebase.auth

        btnSignIn.setOnClickListener {
            val email: String = etEmail.text.toString()
            val password: String = etPassword.text.toString()

            if (email.isEmpty()) {
                etEmail.requestFocus()
                etEmail.setError("Email is required")
                return@setOnClickListener
            } else if (password.isEmpty()) {
                etPassword.requestFocus()
                etPassword.setError("Password is required")
                return@setOnClickListener
            }

            signInWithEmailPassword(email, password)
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
//        Navigate to home activity if user not null
        if (user == null) {
            Log.w(TAG, "User is null")
            return
        }
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun signInWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.d(TAG, "signInWithEmail:failure", task.exception)
                    etPassword.requestFocus()
                    etPassword.setError("Incorrect email or password")
                    updateUI(null)
                }
            }
    }

}