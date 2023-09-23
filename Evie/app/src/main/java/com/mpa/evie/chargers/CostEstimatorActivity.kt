package com.mpa.evie.chargers

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.mpa.evie.R
import com.mpa.evie.utils.getCostPerKwhFromString

class CostEstimatorActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "CostEstimatorActivity"
    }

    private lateinit var tvEstimatedCost: TextView
    private lateinit var tvCost: TextView
    private lateinit var tvCETitle: TextView
    private lateinit var sliderSOC: Slider
    private lateinit var sliderTargetCharge: Slider

    private var chargerCost: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cost_estimator)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        tvEstimatedCost = findViewById(R.id.tvEstimatedCost)
        tvCost = findViewById(R.id.tvCost)
        tvCETitle = findViewById(R.id.tvCETitle)
        sliderSOC = findViewById(R.id.sliderSOC)
        sliderTargetCharge = findViewById(R.id.sliderTargetCharge)

        chargerCost = intent.extras!!.getString("chargerCost")
        tvCETitle.text = intent.extras!!.getString("chargerTitle")
        tvCost.text = chargerCost
        sliderSOC.addOnChangeListener { _, value, _ ->
            updateEstimatedCost(value.toInt(), sliderTargetCharge.value.toInt())
        }
        sliderTargetCharge.addOnChangeListener { _, value, _ ->
            updateEstimatedCost(
                sliderSOC.value.toInt(),
                value.toInt()
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateEstimatedCost(soc: Int, targetCharge: Int) {
        if (targetCharge <= soc) {
            tvEstimatedCost.text = "£" + 0.00
            return
        }
        val diff = targetCharge - soc
        val costPerKwh: Double = getCostPerKwhFromString(chargerCost) ?: 0.0

        val overallCost = when {
            diff < 10 -> costPerKwh
            diff >= 10 && diff < 30 -> costPerKwh * 1.5
            diff >= 30 && diff < 50 -> costPerKwh * 2
            diff >= 50 && diff < 70 -> costPerKwh * 2.5
            else -> costPerKwh * 3
        }
        tvEstimatedCost.text = "£" + String.format("%.2f", overallCost).toDouble()
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