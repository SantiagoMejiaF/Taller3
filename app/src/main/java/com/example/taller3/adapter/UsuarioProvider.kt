package com.example.taller3.adapter

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.taller3.MapaLocalizaciones
import com.example.taller3.Usuario
import com.google.firebase.database.*

class UsuarioProvider {
    interface UsuariosCallback {
        fun onUsuariosLoaded(usuarios: List<Usuario>)
        fun onError(message: String)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private lateinit var usuarioAdapter: UsuarioAdapter
        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun obtenerUsuarios(callback: UsuariosCallback) {
            val mRootReference = FirebaseDatabase.getInstance().reference.child("usuarios")
            val listaUsuarios = mutableListOf<Usuario>()

            mRootReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val usuario = snapshot.getValue(Usuario::class.java)
                        usuario?.let {
                            listaUsuarios.add(it)
                        }
                    }

                    val usuariosDisponibles = listaUsuarios.filter { it.estado == "Disponible" }
                    callback.onUsuariosLoaded(usuariosDisponibles)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback.onError("Error al leer los usuarios: ${databaseError.toException()}")
                }
            })
        }

        fun establecerAdapter(adapter: UsuarioAdapter) {
            usuarioAdapter = adapter
        }

        fun obtenerAdapter() = usuarioAdapter

        fun suscribirCambiosEstadoUsuarios(context: Context) {
            this.context = context
            val mRootReference = FirebaseDatabase.getInstance().reference.child("usuarios")
            mRootReference.addChildEventListener(object : ChildEventListener {
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuario?.let {
                        if (it.estado == "Disponible") {
                            val usuariosActivos = snapshot.childrenCount.toInt()
                            actualizarNotificacion(it, usuariosActivos)
                        }
                    }
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}

                private fun actualizarNotificacion(usuario: Usuario, usuariosActivos: Int) {
                    val channelId = "usuarios_channel"
                    val channelName = "Usuarios Channel"
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channel =
                        NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                    notificationManager.createNotificationChannel(channel)

                    val notificationBuilder = NotificationCompat.Builder(
                        context,
                        channelId
                    )
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Usuarios Disponibles")
                        .setContentText("Usuario ${usuario.nombre} se ha conectado.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                    val intent = Intent(context, MapaLocalizaciones::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    notificationBuilder.setContentIntent(pendingIntent)

                    val notification = notificationBuilder.build()
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            })
        }
    }
}
