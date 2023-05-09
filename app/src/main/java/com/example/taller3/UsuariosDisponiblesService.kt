package com.example.taller3

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class UsuariosDisponiblesService : Service() {

    private val NOTIFICATION_ID = 1

    private lateinit var usuariosRef: DatabaseReference
    private lateinit var usuariosListener: ValueEventListener
    private lateinit var auth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()

        usuariosRef = FirebaseDatabase.getInstance().reference.child("usuarios")
        auth = Firebase.auth

        usuariosListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val usuarios = dataSnapshot.children.mapNotNull { snapshot ->
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuario?.apply { uid = snapshot.key ?: "" }
                }
                val usuarioActual = auth.currentUser
                if (usuarioActual != null) {
                    val usuariosDisponibles = usuarios.filter { it.uid != usuarioActual.uid }
                    val intent = Intent("usuarios_disponibles")
                    intent.putExtra("usuarios", usuariosDisponibles.toTypedArray())
                    sendBroadcast(intent)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, "Error al leer los usuarios", Toast.LENGTH_SHORT).show()
            }
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        usuariosRef.addValueEventListener(usuariosListener)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        usuariosRef.removeEventListener(usuariosListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createNotification(): Notification {

        val channelId = "usuarios_channel"
        val channelName = "Usuarios Channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        // Construir la notificación
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icono_usuario)
            .setContentTitle("Usuarios Disponibles")
            .setContentText("Monitoreando usuarios disponibles")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Abrir la actividad principal al hacer clic en la notificación
        val intent = Intent(this, MapaLocalizaciones::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder.setContentIntent(pendingIntent)

        // Construir la notificación y devolverla
        return notificationBuilder.build()
    }
}