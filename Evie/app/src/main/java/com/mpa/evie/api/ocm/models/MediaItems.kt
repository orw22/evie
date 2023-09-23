package com.mpa.evie.api.ocm.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MediaItems(
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("ChargePointID") var ChargePointID: Int? = null,
    @SerializedName("ItemURL") var ItemURL: String? = null,
    @SerializedName("ItemThumbnailURL") var ItemThumbnailURL: String? = null,
    @SerializedName("Comment") var Comment: String? = null,
    @SerializedName("IsEnabled") var IsEnabled: Boolean? = null,
    @SerializedName("IsVideo") var IsVideo: Boolean? = null,
    @SerializedName("IsFeaturedItem") var IsFeaturedItem: Boolean? = null,
    @SerializedName("IsExternalResource") var IsExternalResource: Boolean? = null,
    @SerializedName("MetadataValue") var MetadataValue: String? = null,
    @SerializedName("DateCreated") var DateCreated: String? = null
) : Serializable