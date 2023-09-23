package com.mpa.evie.info

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mpa.evie.MapsActivity
import com.mpa.evie.R
import com.mpa.evie.data.networks
import com.mpa.evie.models.Network

class NetworksActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "NetworksActivity"
    }

    private lateinit var rvNetworks: RecyclerView
    private lateinit var btnNetworksContinue: Button

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var userNetworks = mutableSetOf<Network>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_networks)

        rvNetworks = findViewById(R.id.rvNetworks)
        rvNetworks.setNestedScrollingEnabled(false)
        btnNetworksContinue = findViewById(R.id.btnNetworksContinue)

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class NetworkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val cb: MaterialCheckBox

                init {
                    cb = view.findViewById(R.id.cb)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkViewHolder {
                val view = LayoutInflater.from(this@NetworksActivity)
                    .inflate(R.layout.rv_networks_row, parent, false)
                return NetworkViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val cb: MaterialCheckBox = holder.itemView.findViewById(R.id.cb)
                val network = networks[position]
                cb.text = network.name
                cb.contentDescription = network.name
                cb.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) userNetworks.add(network) else userNetworks.remove(network)
                }
            }

            override fun getItemCount(): Int {
                return networks.size
            }
        }

        val layoutManager = LinearLayoutManager(this)
        rvNetworks.adapter = adapter
        rvNetworks.layoutManager = layoutManager

        val query = db.collection("users").document(auth.currentUser!!.uid)

        btnNetworksContinue.setOnClickListener {
            query.update("networks", userNetworks.toList()) // update user in db
            startActivity(
                Intent(
                    applicationContext,
                    MapsActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            ) // go to map
            finish()
        }
    }
}