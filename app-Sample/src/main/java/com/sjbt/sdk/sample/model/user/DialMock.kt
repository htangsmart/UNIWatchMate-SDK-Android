package com.sjbt.sdk.sample.model.user

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by qiyachao
 * on 2023_10_11
 */
data class DialMock(val dialCoverRes:Int, val dialAssert: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(dialCoverRes)
        dest.writeString(dialAssert)
    }

    companion object CREATOR : Parcelable.Creator<DialMock> {
        override fun createFromParcel(parcel: Parcel): DialMock {
            return DialMock(parcel)
        }

        override fun newArray(size: Int): Array<DialMock?> {
            return arrayOfNulls(size)
        }
    }
}