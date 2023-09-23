package com.mpa.evie.routes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mpa.evie.R
import com.mpa.evie.models.Route
import com.mpa.evie.models.User

class RoutesActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RoutesActivity"
    }

    private val SECONDS_IN_MIN = 60

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var routes = arrayListOf<Route>()
    private lateinit var rvRoutes: RecyclerView
    private lateinit var tvNoRoutes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routes)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        tvNoRoutes = findViewById(R.id.tvNoRoutes)
        rvRoutes = findViewById(R.id.rvRoutes)
        rvRoutes.setNestedScrollingEnabled(false)

        val query = db.collection("users").document(auth.currentUser!!.uid)

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val tvName: TextView
                val tvStartToEnd: TextView
                val tvDistance: TextView
                val tvDuration: TextView
                val btnDelete: ImageButton

                init {
                    tvName = view.findViewById(R.id.tvName)
                    tvStartToEnd = view.findViewById(R.id.tvStartToEnd)
                    tvDistance = view.findViewById(R.id.tvDistance)
                    tvDuration = view.findViewById(R.id.tvDuration)
                    btnDelete = view.findViewById(R.id.btnDelete)
                }
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RouteViewHolder {
                val view = LayoutInflater.from(this@RoutesActivity)
                    .inflate(R.layout.rv_routes_row, parent, false)
                return RouteViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tvName: TextView = holder.itemView.findViewById(R.id.tvName)
                val tvStartToEnd: TextView = holder.itemView.findViewById(R.id.tvStartToEnd)
                val tvDistance: TextView = holder.itemView.findViewById(R.id.tvDistance)
                val tvDuration: TextView = holder.itemView.findViewById(R.id.tvDuration)
                val btnDelete: ImageButton = holder.itemView.findViewById(R.id.btnDelete)

                tvName.text = routes[position].name
                tvStartToEnd.text =
                    routes[position].origin?.name + " -> " + routes[position].destination?.name
                tvDistance.text = "${routes[position].distance} mi"
                tvDuration.text = "${routes[position].duration?.div(SECONDS_IN_MIN)}m"
                holder.itemView.contentDescription = routes[position].name

                btnDelete.setOnClickListener {
                    query.update("routes", FieldValue.arrayRemove(routes[position]))
                    Toast.makeText(this@RoutesActivity, "Route deleted", Toast.LENGTH_SHORT).show()
                }
            }

            override fun getItemCount(): Int {
                return routes.size
            }
        }

        val layoutManager = LinearLayoutManager(this)
        rvRoutes.adapter = adapter
        rvRoutes.layoutManager = layoutManager

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Get user failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject<User>()
                Log.d(TAG, user.toString())
                if (user?.routes != null) {
                    routes = user.routes
                    adapter.notifyDataSetChanged()
                    if (routes.isEmpty()) {
                        rvRoutes.visibility = View.GONE
                        tvNoRoutes.visibility = View.VISIBLE
                    }
                }
            }
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