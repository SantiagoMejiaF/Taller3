package com.example.taller3
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.OutputStream
import java.util.*
import com.example.taller3.databinding.RegistroFotoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class RegistroFoto : AppCompatActivity() {

    private lateinit var binding: RegistroFotoBinding
    private lateinit var file: File

    @RequiresApi(Build.VERSION_CODES.Q)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = RegistroFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFoto.setOnClickListener {
            verificarPermisoCamara()
        }

        binding.btnGaleria.setOnClickListener {
            selectImageFromGallery()
        }

        binding.btnContinuar.setOnClickListener {
            guardarEnGaleria()
            val intent = Intent(this, MapaLocalizaciones::class.java)
            startActivity(intent)
        }
    }

    // Subir una foto a Firebase Storage

    private fun subirImagen(photoURI: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            val storageRef = Firebase.storage.reference
            val imageRef = storageRef.child("images/$uid.jpg")

            val uploadTask = imageRef.putFile(photoURI)
            uploadTask.addOnSuccessListener {
                // Imagen subida exitosamente
                Toast.makeText(this, "Imagen subida exitosamente al storage de Firebase", Toast.LENGTH_LONG).show()

                // Actualizar el campo 'foto' del usuario en la base de datos
                val userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
                userRef.child("foto").setValue(imageRef.path)
            }.addOnFailureListener {
                // Error al subir la imagen
                Toast.makeText(this, "Error al subir la imagen al storage de Firebase", Toast.LENGTH_LONG).show()
            }
        } else {
            // No hay usuario autenticado
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_LONG).show()
        }
    }


    // Guardar una foto en la galeria

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun guardarEnGaleria() {
        val contenedor = crearContenedor()
        val uri = guardarImagen(contenedor)
        limpiarContenedor(contenedor, uri)
        Toast.makeText(this,"Imagen guardada en la galeria",Toast.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun crearContenedor(): ContentValues {

        val nombreArchivo = file.name
        val tipoArchivo = "image/jpg"
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.Files.FileColumns.MIME_TYPE, tipoArchivo)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    private fun guardarImagen(contenedor: ContentValues): Uri {
        var outputStream: OutputStream?
        var uri: Uri?

        application.contentResolver.also { resolver ->
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contenedor)
            outputStream = resolver.openOutputStream(uri!!)
        }
        outputStream?.use { output ->
            obtenerBitMap().compress(Bitmap.CompressFormat.JPEG, 100, output)
        }
        return uri!!
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun limpiarContenedor(contenedor: ContentValues, uri: Uri) {
        contenedor.clear()
        contenedor.put(MediaStore.MediaColumns.IS_PENDING, 0)
        contentResolver.update(uri, contenedor, null, null)
    }


    // Tomar una foto desde la camara

    private val abrirCamara =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = obtenerBitMap()
                val img = findViewById<ImageView>(R.id.img)
                val matrix = Matrix()
                matrix.postRotate(90f)
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                img.setImageBitmap(rotatedBitmap)
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            pedirPermisoCamara()
        }else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                it.resolveActivity(packageManager)?.also { _ ->
                    createPhotoFile()
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.taller3.fileprovider", file)
                    it.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }
            }
            abrirCamara.launch(intent)
        }
    }

    private fun pedirPermisoCamara(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            Toast.makeText(this, "Permisos rechazados", Toast.LENGTH_LONG).show()
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }
    }

    private fun createPhotoFile() {
        val directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        file = File.createTempFile("IMG_${System.currentTimeMillis()}_", ".jpg", directorio)
    }

    private fun obtenerBitMap(): Bitmap {
        return BitmapFactory.decodeFile(file.toString())
    }

    // Escoger una foto desde la galeria

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Mostrar la imagen en el ImageView
        val imageView = findViewById<ImageView>(R.id.img)
        imageView.setImageURI(uri)
    }

    private fun selectImageFromGallery() {
        // Verificar si se tiene el permiso de acceso a la galería
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Se tiene el permiso, mostrar el selector de imágenes de la galería
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
            pickImage.launch("image/*")
        } else {
            // No se tiene el permiso, solicitarlo
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
        }
    }

    // Solicitar permisos
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso para tomar una foto concedido
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                        it.resolveActivity(packageManager)?.also { _ ->
                            createPhotoFile()
                            val photoURI: Uri = FileProvider.getUriForFile(
                                this,
                                "com.example.taller3.fileprovider", file
                            )
                            it.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            subirImagen(photoURI)
                        }
                    }
                    abrirCamara.launch(intent)
                } else {
                    // Permiso para tomar una foto denegado
                    Toast.makeText(this, "El permiso para tomar una foto fue denegado", Toast.LENGTH_SHORT).show()
                }
            }
            2 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, mostrar el selector de imágenes de la galería
                    pickImage.launch("image/*")
                } else {
                    // Permiso denegado, mostrar mensaje al usuario
                    Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}