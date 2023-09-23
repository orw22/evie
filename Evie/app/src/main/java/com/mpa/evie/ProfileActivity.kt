package com.mpa.evie

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.mpa.evie.account.StartActivity
import com.mpa.evie.info.NameActivity
import com.mpa.evie.models.User
import com.mpa.evie.settings.SettingsActivity

class ProfileActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "ProfileActivity"
    }

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var gson = Gson()

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var btnSettings: FloatingActionButton
    private lateinit var tvDisplayName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvCar: TextView
    private lateinit var tvChargingNetworks: TextView

    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        getSupportActionBar()?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val currentUser = auth.currentUser

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE)

        tvDisplayName = findViewById(R.id.tvDisplayName)
        tvEmail = findViewById(R.id.tvEmail)
        tvCar = findViewById(R.id.tvCar)
        tvChargingNetworks = findViewById(R.id.tvChargingNetworks)
        btnSettings = findViewById(R.id.btnSettings)

        val query = db.collection("users").document(currentUser!!.uid)

        if (currentUser == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        } else if (currentUser.displayName.isNullOrEmpty()) {
//                    if display name not set
            startActivity(Intent(this, NameActivity::class.java))
        }

        tvDisplayName.text = currentUser.displayName ?: ""
        tvEmail.text = currentUser.email

        val userJson = sharedPreferences.getString("user", null)
        if (!userJson.isNullOrEmpty()) {
            user = gson.fromJson(userJson, User::class.java)
        } else {
            query.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "User query listener failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    user = snapshot.toObject<User>()
                    with(sharedPreferences.edit()) {
                        putString("user", gson.toJson(user))
                        apply()
                    }
                } else {
                    Log.d(TAG, "User data not found")
                }
            }
        }

        if (user?.car?.make != null && user?.car?.model != null) {
            tvCar.text = (user?.car?.make) + " " + (user?.car?.model)
        } else {
            tvCar.text = "Not set"
        }

        if (!user?.networks.isNullOrEmpty()) {
            tvChargingNetworks.text = user?.networks?.map { it.name }?.joinToString()
        } else {
            tvChargingNetworks.text = "Not set"
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.selectedItemId = R.id.navigation_profile

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                }
                R.id.navigation_maps -> {
                    startActivity(Intent(applicationContext, MapsActivity::class.java))
                    overridePendingTransition(0, 0)
                }
            }
            false
        }
    }
}