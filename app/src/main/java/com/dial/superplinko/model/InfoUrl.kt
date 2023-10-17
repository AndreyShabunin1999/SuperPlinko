package com.dial.superplinko.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class InfoUrl (
    @SerializedName("url") var url : String? = null
)
