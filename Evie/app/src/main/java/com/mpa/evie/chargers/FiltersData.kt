package com.mpa.evie.chargers

data class FiltersData(
    var minPorts: Int? = null,
    var maxCostPerKwh: Double? = null,
    var speedSlow: Boolean = true,
    var speedFast: Boolean = true
)
