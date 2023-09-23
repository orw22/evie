package com.mpa.evie.api.ocm.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UsageType(
    @SerializedName("IsPayAtLocation") var IsPayAtLocation: Boolean? = null,
    @SerializedName("IsMembershipRequired") var IsMembershipRequired: Boolean? = null,
    @SerializedName("IsAccessKeyRequired") var IsAccessKeyRequired: Boolean? = null,
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("Title") var Title: String? = null
) : Serializable
