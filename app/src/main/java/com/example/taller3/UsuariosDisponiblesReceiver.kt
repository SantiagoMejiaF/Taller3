package com.example.taller3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.taller3.adapter.UsuarioProvider


class UsuariosDisponiblesReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "usuarios_disponibles") {
            val usuarios = intent.getParcelableArrayExtra("usuarios")?.map { it as Usuario }
            if (usuarios != null) {
                val adapter = UsuarioProvider.obtenerAdapter()
                adapter?.actualizarUsuarios(usuarios)
            }
        }
    }
}