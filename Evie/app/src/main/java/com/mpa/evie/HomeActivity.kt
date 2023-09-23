package com.mpa.evie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mpa.evie.BuildConfig.OCM_API_KEY
import com.mpa.evie.api.ocm.OpenChargeMapAPI
import com.mpa.evie.api.ocm.models.Charger
import com.mpa.evie.chargers.ChargerActivity
import com.mpa.evie.info.NameActivity
import com.mpa.evie.models.User
import com.mpa.evie.routes.CreateRouteActivity
import com.mpa.evie.routes.RoutesActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class HomeActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var retrofit: Retrofit
    private lateinit var ocmApi: OpenChargeMapAPI

    private lateinit var tvTitle: TextView
    private lateinit var btnCharger: Button
    private lateinit var btnPlanRoute: Button
    private lateinit var btnRoutes: Button
    private lateinit var rvFavourites: RecyclerView

    private var favourites: List<Charger?> = listOf()

    private companion object {
        private const val TAG = "HomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tvTitle = findViewById(R.id.tvTitle)
        btnCharger = findViewById(R.id.btnCharger)
        btnPlanRoute = findViewById(R.id.btnPlanRoute)
        btnRoutes = findViewById(R.id.btnRoutes)
        rvFavourites = findViewById(R.id.rvFavourites)
        rvFavourites.setNestedScrollingEnabled(false)

        retrofit = Retrofit.Builder()
            .baseUrl(MapsActivity.OCM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        ocmApi = retrofit.create(OpenChargeMapAPI::class.java)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: not signed in", Toast.LENGTH_SHORT).show()
        }

        if (currentUser?.displayName.isNullOrEmpty()) {
            startActivity(Intent(this, NameActivity::class.java))
        }

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val titleStart = when {
            hour < 12 -> "Good morning, "
            hour >= 12 && hour < 18 -> "Good afternoon, "
            else -> "Good evening, "
        }

        tvTitle.text = titleStart + currentUser?.displayName!!.split(" ").get(0)
        btnCharger.setOnClickListener {
            startActivity(Intent(applicationContext, MapsActivity::class.java))
        }
        btnPlanRoute.setOnClickListener {
            startActivity(Intent(applicationContext, CreateRouteActivity::class.java))
        }
        btnRoutes.setOnClickListener {
            startActivity(Intent(applicationContext, RoutesActivity::class.java))
        }

//        Setup bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.selectedItemId = R.id.navigation_home // home activity selected

        // Add nav item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                }
                R.id.navigation_maps -> {
                    startActivity(Intent(applicationContext, MapsActivity::class.java))
                    overridePendingTransition(0, 0)
                }
            }
            false
        }

        val query = db.collection("users").document(auth.currentUser!!.uid)
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class ChargerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val tvName: TextView
                val tvDescription: TextView

                init {
                    tvName = view.findViewById(R.id.tvName)
                    tvDescription = view.findViewById(R.id.tvDescription)
                }
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ChargerViewHolder {
                val view = LayoutInflater.from(this@HomeActivity)
                    .inflate(R.layout.rv_charger_row, parent, false)
                return ChargerViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tvName: TextView = holder.itemView.findViewById(R.id.tvName)
                val tvDescription: TextView = holder.itemView.findViewById(R.id.tvDescription)

                tvName.text = favourites[position]?.AddressInfo?.Title

                tvDescription.text =
                    "${favourites[position]?.NumberOfPoints} ports, ${
                        when (favourites[position]?.StatusTypeID) {
                            50 -> "Operational"
                            else -> "Out of order"
                        }
                    }, ${favourites[position]!!.UsageCost}"
                holder.itemView.contentDescription = favourites[position]?.AddressInfo?.Title

                holder.itemView.setOnClickListener {
                    startActivity(
                        Intent(this@HomeActivity, ChargerActivity::class.java).putExtra(
                            "charger",
                            favourites[position]
                        )
                    )
                }
            }

            override fun getItemCount(): Int {
                return favourites.size
            }
        }

        val layoutManager = LinearLayoutManager(this)
        rvFavourites.adapter = adapter
        rvFavourites.layoutManager = layoutManager

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "User query listener failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject<User>()
                val chargePointIds = user!!.favourites.joinToString(",")

                if (!chargePointIds.isEmpty()) {
                    ocmApi.getChargersById(
                        OCM_API_KEY,
                        chargePointIds
                    )
                        ?.enqueue(object : Callback<List<Charger?>?> {
                            override fun onResponse(
                                call: Call<List<Charger?>?>,
                                response: Response<List<Charger?>?>
                            ) {
                                favourites =
                                    response.body() ?: listOf()
                                adapter.notifyDataSetChanged()
                            }

                            override fun onFailure(call: Call<List<Charger?>?>, t: Throwable) {
                                Log.e(TAG, t.message ?: "")
                            }
                        })
                }

            } else {
                Log.e(TAG, "User data not found")
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.miLogout) {
//            Log.i(TAG, "Log out")
//            auth.signOut()
//            val logoutIntent = Intent(this, StartActivity::class.java)
//            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(logoutIntent)
//        }
//        return super.onOptionsItemSelected(item)
//    }

}
