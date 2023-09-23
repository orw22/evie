package com.mpa.evie.chargers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mpa.evie.ProfileActivity
import com.mpa.evie.R
import com.mpa.evie.api.ocm.models.Charger
import com.mpa.evie.api.ocm.models.Connections
import com.mpa.evie.models.User
import com.squareup.picasso.Picasso


class ChargerActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ChargerActivity"
    }

    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth

    private var charger: Charger? = null
    private var isFavourite: Boolean = false

    private var connections = arrayListOf<Connections>()

    private lateinit var tvChargerName: TextView
    private lateinit var ivChargerImage: ImageView
    private lateinit var tvAddress: TextView
    private lateinit var btnFavourites: ImageButton
    private lateinit var btnDirections: Button
    private lateinit var btnEstimateCost: Button
    private lateinit var tvDescription: TextView
    private lateinit var rvConnections: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charger)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        auth = Firebase.auth
        charger = intent.extras!!.getSerializable("charger") as Charger

        val query = db.collection("users").document(auth.currentUser!!.uid)

        tvChargerName = findViewById(R.id.tvChargerName)
        ivChargerImage = findViewById(R.id.ivChargerImage)
        tvDescription = findViewById(R.id.tvDescription)
        tvAddress = findViewById(R.id.tvAddress)
        btnFavourites = findViewById(R.id.btnFavourites)
        btnDirections = findViewById(R.id.btnDirections)
        btnEstimateCost = findViewById(R.id.btnEstimateCost)
        rvConnections = findViewById(R.id.rvConnections)
        rvConnections.setNestedScrollingEnabled(false)

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class ConnectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val tvQuantity: TextView
                val tvType: TextView
                val tvVoltage: TextView
                val tvOperational: TextView

                init {
                    tvQuantity = view.findViewById(R.id.tvQuantity)
                    tvType = view.findViewById(R.id.tvType)
                    tvVoltage = view.findViewById(R.id.tvVoltage)
                    tvOperational = view.findViewById(R.id.tvOperational)
                }
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ConnectionViewHolder {
                val view = LayoutInflater.from(this@ChargerActivity)
                    .inflate(R.layout.rv_connections_row, parent, false)
                return ConnectionViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tvQuantity: TextView = holder.itemView.findViewById(R.id.tvQuantity)
                val tvType: TextView = holder.itemView.findViewById(R.id.tvType)
                val tvVoltage: TextView = holder.itemView.findViewById(R.id.tvVoltage)
                val tvOperational: TextView = holder.itemView.findViewById(R.id.tvOperational)

                tvQuantity.text = "${connections[position].Quantity.toString()}x"
                tvType.text = connections[position].ConnectionType?.Title
                tvVoltage.text = "${connections[position].PowerKW}V"
                tvOperational.text = connections[position].StatusType?.Title

                holder.itemView.contentDescription =
                    "${connections[position].Quantity.toString()}x ${connections[position].ConnectionType?.Title}"
            }

            override fun getItemCount(): Int {
                return connections.size
            }
        }

        val layoutManager = LinearLayoutManager(this)
        rvConnections.adapter = adapter
        rvConnections.layoutManager = layoutManager

        btnFavourites.setOnClickListener {
            if (isFavourite) {
//                remove from favourites
                query.update("favourites", FieldValue.arrayRemove(charger?.ID))
                    .addOnSuccessListener { _ ->
                        changeFavourite(false)
                    }.addOnFailureListener { exception ->
                        Log.d(TAG, exception.message.toString())
                    }
            } else {
//                add to favourites
                query.update("favourites", FieldValue.arrayUnion(charger?.ID))
                    .addOnSuccessListener { _ ->
                        changeFavourite(true)
                    }.addOnFailureListener { exception ->
                        Log.d(TAG, exception.message.toString())
                    }
            }
        }

        connections = charger!!.Connections
        adapter.notifyDataSetChanged()

        tvAddress.text =
            "" + charger?.AddressInfo?.AddressLine1 + ", " + charger?.AddressInfo?.Town + " " + charger?.AddressInfo?.Postcode
        tvChargerName.text = charger?.AddressInfo?.Title
        tvDescription.text = charger?.GeneralComments

        if (charger?.GeneralComments.isNullOrEmpty()) tvDescription.visibility = View.GONE
        if (!charger?.MediaItems.isNullOrEmpty()) {
            Picasso.get().load(charger!!.MediaItems[0]!!.ItemURL).into(ivChargerImage)
        }

        btnDirections.setOnClickListener {
            val mapIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "geo:" + charger?.AddressInfo?.Latitude + "," + charger?.AddressInfo?.Longitude + "?q=" + charger?.AddressInfo?.Title?.replace(
                        " ",
                        "+"
                    )
                )
            )
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent) // launch google maps
        }
        btnEstimateCost.setOnClickListener {
            startActivity(
                Intent(applicationContext, CostEstimatorActivity::class.java).putExtra(
                    "chargerTitle",
                    charger?.AddressInfo?.Title
                ).putExtra("chargerCost", charger?.UsageCost)
            )
        }

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "User query listener failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject<User>()
                if (user?.favourites?.contains(charger?.ID)!!) {
                    changeFavourite(true, true)
                }
            } else {
                Log.d(TAG, "User data not found")
            }
        }

    }

    private fun changeFavourite(value: Boolean, isDefault: Boolean = false) {
        if (value) {
            isFavourite = true
            btnFavourites.setImageResource(R.drawable.ic_star) // fill in star icon
            if (!isDefault) {
                Toast.makeText(this, "Added to favourites", Toast.LENGTH_SHORT).show()
            }
        } else {
            isFavourite = false
            btnFavourites.setImageResource(R.drawable.ic_star_border)
            if (!isDefault) {
                Toast.makeText(this, "Removed from favourites", Toast.LENGTH_SHORT).show()
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