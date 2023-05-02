package com.example.taller3

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.service.controls.ControlsProviderService.TAG
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3.databinding.RegistroDatosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

const val PATH_USERS = "usuarios/"

class RegistroDatos : AppCompatActivity() {

    private lateinit var binding: RegistroDatosBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var myRef: DatabaseReference
    private val database = FirebaseDatabase.getInstance()
    private val myRefUsers = database.getReference(PATH_USERS)


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = RegistroDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnContinuar.setOnClickListener {
            registroUsuario(binding.etEmailUsuario.text.toString(), binding.etPasswordUsuario.text.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun registroUsuario (email: String, password:String){
        if (validateForm()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                        val user = auth.currentUser
                        if (user != null) {
                            val upcrb = UserProfileChangeRequest.Builder()
                            upcrb.setDisplayName(binding.etNombreUsuario.text.toString() + " " + binding.etApellidoUsuario.text.toString())
                            upcrb.setPhotoUri(Uri.parse("path/to/pic")) //fake uri, use Firebase Storage
                            user.updateProfile(upcrb.build())
                            updateUI(user)
                            enviarFireBase()
                        }
                    } else {
                        Toast.makeText(
                            this, "createUserWithEmail:Failure: " + task.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        task.exception?.message?.let { Log.e(TAG, it) }
                    }
                }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        if (!email.contains("@") ||
            !email.contains(".") ||
            email.length < 5)
            return false
        return true
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = binding.etEmailUsuario.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.etEmailUsuario.error = "Este campo es obligatorio."
            valid = false
        } else {
            isEmailValid(email)
            binding.etEmailUsuario.error = null
        }
        val password = binding.etPasswordUsuario.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.etPasswordUsuario.error = "Este campo es obligatorio."
            valid = false
        } else {
            binding.etPasswordUsuario.error = null
        }
        return valid
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            updateUI(currentUser)
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(this, RegistroFoto::class.java)
            intent.putExtra("user", currentUser.email)
            startActivity(intent)
        } else {
            binding.etEmailUsuario.setText("")
            binding.etPasswordUsuario.setText("")
        }
    }

    private fun enviarFireBase (){

        myRef = myRefUsers.child(auth.currentUser?.uid.toString())

        val nuevoUsuario = Usuario()
        nuevoUsuario.setNombre(binding.etNombreUsuario.text.toString())
        nuevoUsuario.setApellido(binding.etApellidoUsuario.text.toString())
        nuevoUsuario.setIdentificacion(binding.etIdentificacionUsuario.text.toString())
        nuevoUsuario.setLatitud(binding.etLatitud.text.toString())
        nuevoUsuario.setLongitud(binding.etLongitud.text.toString())
        nuevoUsuario.setUid(auth.currentUser?.uid.toString())

        myRef.setValue(nuevoUsuario)
    }
}
