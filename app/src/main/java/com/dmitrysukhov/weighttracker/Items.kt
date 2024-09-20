package com.dmitrysukhov.weighttracker

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val date: Long, val weight: Float,
    val noSugar: Boolean, val noBread: Boolean, val grams: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(), parcel.readLong(), parcel.readFloat(),
        parcel.readByte() != 0.toByte(), parcel.readByte() != 0.toByte(), parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(date)
        parcel.writeFloat(weight)
        parcel.writeByte(if (noSugar) 1 else 0)
        parcel.writeByte(if (noBread) 1 else 0)
        parcel.writeInt(grams)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<WeightEntry> {
        override fun createFromParcel(parcel: Parcel) = WeightEntry(parcel)
        override fun newArray(size: Int): Array<WeightEntry?> = arrayOfNulls(size)
    }
}