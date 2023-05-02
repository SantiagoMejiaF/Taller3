package com.example.taller3

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.google.firebase.database.*

// Servicio
class UserAvailabilityService : Service() {

    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var usersListener: ValueEventListener

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")

        usersListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(Usuario::class.java)
                    if (user?.getEstado() == "Disponible") {
                        Toast.makeText(applicationContext, "${user.getNombre()} se ha conectado", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, "Error en la conexión a la base de datos: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        }
        usersRef.addValueEventListener(usersListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        usersRef.removeEventListener(usersListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}