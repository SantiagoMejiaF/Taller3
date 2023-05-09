package com.example.taller3

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taller3.adapter.UsuarioAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taller3.adapter.UsuarioProvider

class ListaUsuarios : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsuarioAdapter
    private lateinit var usuariosReceiver: UsuariosDisponiblesReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_usuarios)

        // Inicializar el RecyclerView y el UsuarioAdapter
        recyclerView = findViewById(R.id.rv_usuarios)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = UsuarioAdapter(emptyList())
        recyclerView.adapter = adapter

        // Iniciar el servicio de usuarios disponibles
        val serviceIntent = Intent(this, UsuariosDisponiblesService::class.java)
        serviceIntent.putExtra("usuariosAdapter", adapter)
        ContextCompat.startForegroundService(this, serviceIntent)

        usuariosReceiver = UsuariosDisponiblesReceiver()
        val filter = IntentFilter("usuarios_disponibles")
        registerReceiver(usuariosReceiver,filter)

        UsuarioProvider.obtenerUsuarios(object : UsuarioProvider.UsuariosCallback {
            override fun onUsuariosLoaded(usuarios: List<Usuario>) {
                adapter.actualizarUsuarios(usuarios)
            }

            override fun onError(message: String) {
                Toast.makeText(this@ListaUsuarios, message, Toast.LENGTH_SHORT).show()
            }
        })

        // Establecer el adaptador antes de suscribirse a los cambios de estado de los usuarios
        UsuarioProvider.establecerAdapter(adapter)
        UsuarioProvider.suscribirCambiosEstadoUsuarios(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usuariosReceiver)
    }
}