package com.mpa.evie.info

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mpa.evie.R
import com.mpa.evie.data.cars
import com.mpa.evie.models.Car


class CarActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "CarActivity"
        val makes =
            listOf("Tesla", "BMW", "Ford", "Audi", "Kia", "Mini", "Nissan", "Hyundai", "Volkswagen")
        val modelMap = mapOf(
            "Tesla" to listOf("Model 3", "Model Y"),
            "BMW" to listOf("I4"),
            "Ford" to listOf("Mustang Mach-E"),
            "Audi" to listOf("e-Tron"),
            "Kia" to listOf("EV6", "e-Niro"),
            "Mini" to listOf("Electric"),
            "Nissan" to listOf("Leaf"),
            "Hyundai" to listOf("Ioniq 5"),
            "Volkswagen" to listOf("ID.3")
        )
    }

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private lateinit var btnCarNext: Button
    private lateinit var actvCarMake: AutoCompleteTextView
    private lateinit var actvCarModel: AutoCompleteTextView
    private lateinit var makeAdapter: ArrayAdapter<String>
    private lateinit var modelAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)

        val query = db.collection("users").document(auth.currentUser!!.uid)

        makeAdapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, makes)
        modelAdapter =
            ArrayAdapter<String>(this, android.R.layout.select_dialog_item, mutableListOf<String>())

        actvCarMake = findViewById<View>(R.id.actvCarMake) as AutoCompleteTextView
        actvCarModel = findViewById<View>(R.id.actvCarModel) as AutoCompleteTextView
        actvCarMake.threshold = 1
        actvCarMake.setAdapter(makeAdapter)
        actvCarModel.threshold = 1
        actvCarModel.setAdapter(modelAdapter)

        actvCarMake.setOnItemClickListener { _, _, position, _ ->
            val value = makeAdapter.getItem(position) ?: ""
            modelAdapter.clear()
            modelMap.get(value)?.apply {
                modelAdapter.addAll(this) // update model options
            }
            actvCarModel.setText("")
            modelAdapter.notifyDataSetChanged()
        }

        btnCarNext = findViewById(R.id.btnCarNext)
        btnCarNext.setOnClickListener {
            if (!makes.contains(actvCarMake.text.toString())) {
                actvCarMake.requestFocus()
                actvCarMake.setError("Please enter a valid car make")
            }
            val selectedCar: Car? = cars.find { c ->
                c.make.equals(actvCarMake.text.toString()) && c.model.equals(
                    actvCarModel.text.toString()
                )
            }
            if (selectedCar != null) {
                query.update("car", selectedCar) // update user's doc
                startActivity(Intent(applicationContext, NetworksActivity::class.java))
            } else {
                actvCarMake.requestFocus()
                actvCarModel.setError("Please enter a valid model")
            }
        }
    }
}