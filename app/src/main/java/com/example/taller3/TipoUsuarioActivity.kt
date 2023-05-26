package com.example.taller3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taller3.databinding.ActivityTipoUsuarioBinding
import com.example.taller3.databinding.LoginBinding

class TipoUsuarioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTipoUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityTipoUsuarioBinding.inflate(layoutInflater)

        binding.buttonUsuario.setOnClickListener {
            val intent = Intent(this, RegistroDatos::class.java)
            startActivity(intent)
        }
        binding.buttonVeterinario.setOnClickListener {
            val intent = Intent(this, RegistroDatosVetActivity::class.java)
            startActivity(intent)
        }
    }
}