package com.mpa.evie.api.ocm.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ConnectionType(

    @SerializedName("FormalName") var FormalName: String? = null,
    @SerializedName("IsDiscontinued") var IsDiscontinued: Boolean? = null,
    @SerializedName("IsObsolete") var IsObsolete: Boolean? = null,
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("Title") var Title: String? = null
) : Serializable
