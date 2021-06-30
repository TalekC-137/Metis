package com.scollon.chattingapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PostModel (val id: String, val fromId: String, val text: String, val timeStamp: Long): Parcelable {
    constructor(): this("", "", "", -1)
}