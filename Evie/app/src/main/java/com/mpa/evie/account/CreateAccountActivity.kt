package com.mpa.evie.account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mpa.evie.HomeActivity
import com.mpa.evie.R

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var btnCreateAccount: Button
    private lateinit var btnBack: FloatingActionButton
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var auth: FirebaseAuth

    private companion object {
        private const val TAG = "CreateAccountActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        getSupportActionBar()?.hide()

        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        btnBack = findViewById(R.id.btnBack)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        auth = Firebase.auth

        btnCreateAccount.setOnClickListener {
            val email: String = etEmail.text.toString()
            val password: String = etPassword.text.toString()
            val confirmPassword: String = etConfirmPassword.text.toString()

            if (email.isEmpty()) {
                etEmail.requestFocus()
                etEmail.setError("Email is required")
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.requestFocus()
                etEmail.setError("Please provide a valid email")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.requestFocus()
                etPassword.setError("Password is required")
                return@setOnClickListener
            } else if (password.length < 8) {
                etPassword.requestFocus()
                etPassword.setError("Password must be at least 8 characters in length")
                return@setOnClickListener
            } else if (!password.equals(confirmPassword)) {
                etConfirmPassword.requestFocus()
                etConfirmPassword.setError("Passwords must match")
                return@setOnClickListener
            }

            createUserWithEmailPassword(email, password)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            Log.w(TAG, "User is null")
            return
        }
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun createUserWithEmailPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                updateUI(auth.currentUser)
            }
    }

}