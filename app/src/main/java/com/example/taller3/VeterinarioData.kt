package com.example.taller3


import android.os.Parcel
import android.os.Parcelable

class VeterinarioData (
    var foto: String? = null,
    var nombre: String? = null,
    var apellido: String? = null,
    var estado: String? = null,
    var placa: String? = null,
    var uid : String? = null

    ): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(foto)
        parcel.writeString(nombre)
        parcel.writeString(apellido)
        parcel.writeString(estado)
        parcel.writeString(placa)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Usuario> {
        override fun createFromParcel(parcel: Parcel): Usuario {
            return Usuario(parcel)
        }

        override fun newArray(size: Int): Array<Usuario?> {
            return arrayOfNulls(size)
        }
    }
}