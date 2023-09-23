package com.mpa.evie.api.ocm.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AddressInfo(
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("Title") var Title: String? = null,
    @SerializedName("AddressLine1") var AddressLine1: String? = null,
    @SerializedName("AddressLine2") var AddressLine2: String? = null,
    @SerializedName("Town") var Town: String? = null,
    @SerializedName("StateOrProvince") var StateOrProvince: String? = null,
    @SerializedName("Postcode") var Postcode: String? = null,
    @SerializedName("CountryID") var CountryID: Int? = null,
    @SerializedName("Latitude") var Latitude: Double? = null,
    @SerializedName("Longitude") var Longitude: Double? = null,
    @SerializedName("ContactTelephone1") var ContactTelephone1: String? = null,
    @SerializedName("ContactTelephone2") var ContactTelephone2: String? = null,
    @SerializedName("ContactEmail") var ContactEmail: String? = null,
    @SerializedName("AccessComments") var AccessComments: String? = null,
    @SerializedName("RelatedURL") var RelatedURL: String? = null,
    @SerializedName("Distance") var Distance: String? = null,
    @SerializedName("DistanceUnit") var DistanceUnit: Int? = null
) : Serializable
