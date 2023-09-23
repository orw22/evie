package com.mpa.evie.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.mpa.evie.R
import com.mpa.evie.account.StartActivity
import com.mpa.evie.routes.RoutesActivity


class SettingsActivity : AppCompatActivity() {

    private lateinit var rvSections: RecyclerView
    private lateinit var btnLogout: Button
    private lateinit var btnDelete: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    private companion object {
        const val TAG = "SettingsActivity"
        val btnLabels = arrayOf(
            "Accessibility",
            "Notifications",
            "Location",
            "Terms and Conditions",
            "Privacy Policy"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        rvSections = findViewById(R.id.rvSections)
        btnLogout = findViewById(R.id.btnLogout)
        btnDelete = findViewById(R.id.btnDelete)
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE)

        auth = Firebase.auth

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val textView: TextView

                init {
                    textView = view.findViewById(R.id.tvSection)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
                val view = LayoutInflater.from(this@SettingsActivity)
                    .inflate(R.layout.rv_settings_row, parent, false)
                return SectionViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tvLabel: TextView = holder.itemView.findViewById(R.id.tvSection)
                tvLabel.text = btnLabels[position]
                holder.itemView.contentDescription = btnLabels[position]

                holder.itemView.setOnClickListener {
                    when (position) {
                        3 -> startActivity(
                            Intent(
                                this@SettingsActivity,
                                TermsAndConditionsActivity::class.java
                            )
                        )
                        4 -> startActivity(
                            Intent(
                                this@SettingsActivity,
                                PrivacyPolicyActivity::class.java
                            )
                        )
                        else -> Toast.makeText(
                            this@SettingsActivity,
                            btnLabels[position],
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }

            override fun getItemCount(): Int {
                return btnLabels.size
            }
        }

        val layoutManager = LinearLayoutManager(this)
        rvSections.adapter = adapter
        rvSections.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            rvSections.getContext(),
            layoutManager.getOrientation()
        )
        rvSections.addItemDecoration(dividerItemDecoration)

        btnLogout.setOnClickListener {
            Log.i(TAG, "Log out")
            auth.signOut()
            val logoutIntent = Intent(this, StartActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            with(sharedPreferences.edit()) {
                remove("user")
                apply()
            }
            startActivity(logoutIntent)
        }

        btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this).setTitle(R.string.delete_account)
                .setMessage(R.string.are_you_sure)

            builder.setPositiveButton(
                R.string.delete_account
            ) { _, _ ->
                auth.currentUser!!.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i(TAG, "User deleted.")
                            startActivity(
                                Intent(
                                    this,
                                    StartActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                        } else {
                            Log.d(TAG, task.exception.toString())
                            Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            builder.setNegativeButton(
                R.string.cancel
            ) { dialog, _ -> dialog.cancel() }

            builder.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}