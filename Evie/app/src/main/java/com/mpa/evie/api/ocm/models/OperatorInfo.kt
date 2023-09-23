package com.mpa.evie.api.ocm.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class OperatorInfo(
    @SerializedName("WebsiteURL") var WebsiteURL: String? = null,
    @SerializedName("Comments") var Comments: String? = null,
    @SerializedName("PhonePrimaryContact") var PhonePrimaryContact: String? = null,
    @SerializedName("PhoneSecondaryContact") var PhoneSecondaryContact: String? = null,
    @SerializedName("IsPrivateIndividual") var IsPrivateIndividual: Boolean? = null,
    @SerializedName("AddressInfo") var AddressInfo: String? = null,
    @SerializedName("BookingURL") var BookingURL: String? = null,
    @SerializedName("ContactEmail") var ContactEmail: String? = null,
    @SerializedName("FaultReportEmail") var FaultReportEmail: String? = null,
    @SerializedName("IsRestrictedEdit") var IsRestrictedEdit: Boolean? = null,
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("Title") var Title: String? = null
) : Serializable