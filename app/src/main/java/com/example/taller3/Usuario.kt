package com.example.taller3

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Usuario {

    private var nombre: String = ""
    private var apellido: String = ""
    private var identificacion: String = ""
    private var latitud: String = ""
    private var longitud: String = ""
    private var imagenUri: Uri? = null
    private var Uid = ""
    private var estado: String = ""

    constructor()

    constructor(
        nombre: String,
        apellido: String,
        identificacion: String,
        latitud: String,
        longitud: String,
        Uid: String,
        estado: String
    ) {
        this.nombre = nombre
        this.apellido = apellido
        this.identificacion = identificacion
        this.latitud = latitud
        this.longitud = longitud
        this.Uid = Uid
        this.estado = estado
    }

    // Getters

    fun getNombre(): String {
        return nombre
    }
    fun setNombre(nombre: String) {
        this.nombre = nombre
    }

    fun getApellido(): String {
        return apellido
    }
    fun setApellido(apellido: String) {
        this.apellido = apellido
    }

    fun getIdentificacion(): String {
        return identificacion
    }
    fun setIdentificacion(identificacion: String) {
        this.identificacion = identificacion
    }

    fun getLatitud(): String {
        return latitud
    }
    fun setLatitud(latitud: String) {
        this.latitud = latitud
    }

    fun getLongitud(): String {
        return longitud
    }
    fun setLongitud(longitud: String) {
        this.longitud = longitud
    }

    fun getImagenUri(): Uri? {
        return imagenUri
    }

    fun setImagenUri(imagenUri: Uri?) {
        this.imagenUri = imagenUri
    }


    fun getUid(): String {
        return Uid
    }

    fun setUid(Uid: String) {
        this.Uid = Uid
    }

    fun getEstado(): String {
        return estado
    }

    fun setEstado(estado: String) {
        this.estado = estado
    }

    fun actualizarEstado(context: Context, estado: String) {

        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        // Verificar si el usuario está autenticado
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("usuarios").document(currentUser.uid)
            userRef.update("estado", estado)
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Estado actualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Error al actualizar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // El usuario no está autenticado, mostrar mensaje de error
            Toast.makeText(
                context,
                "Usuario no autenticado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
