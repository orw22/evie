package com.mpa.evie.chargers

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.mpa.evie.R


class FilterFragment : Fragment() {

    private companion object {
        const val MIN_PORTS = 1
        const val MAX_PORTS = 10
        const val MIN_COST_PER_KWH = 0.0
        const val MAX_COST_PER_KWH = 5.0
    }

    private var minPorts: Int? = null
    private var maxCostPerKwh: Float? = null

    private lateinit var etMinPorts: EditText
    private lateinit var etMaxCostPerKwh: EditText
    private lateinit var btnClose: ImageButton
    private lateinit var cbSpeedSlow: MaterialCheckBox
    private lateinit var cbSpeedFast: MaterialCheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter, container, false)

        etMinPorts = view.findViewById(R.id.etMinPorts)
        etMaxCostPerKwh = view.findViewById(R.id.etMaxCostPerKwh)
        btnClose = view.findViewById(R.id.btnClose)
        cbSpeedSlow = view.findViewById(R.id.cbSpeedSlow)
        cbSpeedFast = view.findViewById(R.id.cbSpeedFast)

        etMinPorts.setText((minPorts ?: 1).toString())
        etMaxCostPerKwh.setText((maxCostPerKwh ?: 5F).toString())
        cbSpeedSlow.isChecked = true
        cbSpeedFast.isChecked = true

        etMinPorts.filters = arrayOf<InputFilter>(MinMaxFilter(MIN_PORTS, MAX_PORTS))
        etMaxCostPerKwh.filters = arrayOf<InputFilter>(DoubleMinMaxFilter(MIN_COST_PER_KWH, MAX_COST_PER_KWH))

        val filters: Filters = getActivity() as Filters

        btnClose.setOnClickListener {
            filters.updateFilters(
                FiltersData(
                    etMinPorts.text.toString().toInt(),
                    etMaxCostPerKwh.text.toString().toDouble(),
                    cbSpeedSlow.isChecked,
                    cbSpeedFast.isChecked
                )
            ) // update filters in activity and close
        }

        return view
    }

    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        constructor(minValue: Int, maxValue: Int) : this() {
            this.intMin = minValue
            this.intMax = maxValue
        }

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dStart: Int,
            dEnd: Int
        ): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

    inner class DoubleMinMaxFilter() : InputFilter {
        private var dblMin: Double = 0.0
        private var dblMax: Double = 0.0

        constructor(minValue: Double, maxValue: Double) : this() {
            this.dblMin = minValue
            this.dblMax = maxValue
        }

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dStart: Int,
            dEnd: Int
        ): CharSequence? {
            try {
                val input = (dest.toString() + source.toString()).toDouble()
                if (isInRange(dblMin, dblMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        private fun isInRange(a: Double, b: Double, c: Double): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }
}