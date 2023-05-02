package com.example.taller3

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class LeerBaseDatos {

    private lateinit var myRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var baseContext: Context

    companion object {
        const val PATH_USERS = "users"
        const val TAG = "LeerBaseDatos"
    }

    fun loadUsers() {
        myRef = database.getReference(PATH_USERS)
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    val myUser = singleSnapshot.getValue(Usuario::class.java)
                    Log.i(TAG, "Encontró usuario: " + myUser?.getNombre())
                    val nombre = myUser?.getNombre()
                    val latitud = myUser?.getLatitud()
                    val longitud = myUser?.getLongitud()

                    val message = "Nombre: $nombre \nLatitud: $latitud \nLongitud: $longitud"
                    Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "error en la consulta", databaseError.toException())
            }
        })
    }

    private fun downloadFile() {
        val localFile = File.createTempFile("images", "jpg")
        val imageRef = storage.reference.child("images/image.jpg")
        imageRef.getFile(localFile)
            .addOnSuccessListener { taskSnapshot ->
                // Successfully downloaded data to local file
                Log.i("FBApp", "successfully downloaded")
                // Update UI using the localFile
            }
            .addOnFailureListener { exception ->
                // Handle failed download
                Log.e("FBApp", "error downloading image", exception)
            }
    }
}