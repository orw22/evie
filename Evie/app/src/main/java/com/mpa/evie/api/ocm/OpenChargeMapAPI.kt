package com.mpa.evie.api.ocm

import com.mpa.evie.api.ocm.models.Charger
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenChargeMapAPI {

    @GET("poi")
    fun getChargers(
        @Query("key") key: String,
        @Query("boundingbox") boundingbox: String,
        @Query("countrycode") countrycode: String = "GB",
        @Query("maxresults") maxresults: Int = 50,
        @Query("connectiontypeid") connectiontypeid: String = "",
        @Query("operatorid") operatorid: String = "",
        @Query("statustypeid") statustypeid: Int = 50 // operational status
    ): Call<List<Charger?>?>?

    @GET("poi")
    fun getChargersById(
        @Query("key") key: String,
        @Query("chargepointid") chargepointid: String
    ): Call<List<Charger?>?>?

}