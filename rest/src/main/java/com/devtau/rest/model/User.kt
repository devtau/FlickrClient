package com.devtau.rest.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
        @SerializedName("id") var id: Long,
        @SerializedName("name") var name: String?,
        @SerializedName("phone") var phone: String?,
        @SerializedName("email") var email: String?,
        @SerializedName("birth_day") var birthDay: String?,
        @SerializedName("sex") var sex: String?,
        @SerializedName("sms_subscription") var smsSubscription: Boolean = true,
        @SerializedName("email_subscription") var emailSubscription: Boolean = true,
        @SerializedName("email_confirmed") var emailConfirmed: String?,
        @SerializedName("phone_confirmed") var phoneConfirmed: String,
        @SerializedName("vk_id") var vkId: String?,
        @SerializedName("fb_id") var fbId: String?,
        @SerializedName("ok_id") var okId: String?,
        @SerializedName("tw_id") var twId: String?,
        @SerializedName("has_franchise") var hasFranchise: Boolean,
        @SerializedName("phone_subscription") var phoneSubscription: Boolean = true,
        @SerializedName("uber") var uber: Boolean,
        @SerializedName("order_count") var orderCount: Int
): Parcelable