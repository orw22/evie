package com.mpa.evie.utils

fun getCostPerKwhFromString(costString: String?): Double? {
    if (costString.isNullOrEmpty()) {
        return null
    }
    return costString.substringAfter("£").substringBefore("/kWh").replace(",", "").toDoubleOrNull()
}