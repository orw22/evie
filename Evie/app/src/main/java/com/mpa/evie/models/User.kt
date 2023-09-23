package com.mpa.evie.models

data class User(
    val displayName: String? = null,
    val car: Car? = null,
    val networks: ArrayList<Network> = arrayListOf(),
    val favourites: ArrayList<Int> = arrayListOf(), // list of charger IDs
    val routes: ArrayList<Route> = arrayListOf()
)
