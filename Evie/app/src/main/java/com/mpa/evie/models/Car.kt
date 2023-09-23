package com.mpa.evie.models

data class Car(
    val connectors: ArrayList<Int> = arrayListOf(), // connector IDs
    val make: String? = null,
    val model: String? = null,
    val range: Int? = null,
    val seats: Int? = null
)
