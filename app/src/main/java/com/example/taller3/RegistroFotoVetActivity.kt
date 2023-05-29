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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.taller3.databinding.ActivityRegistroFotoVetBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.OutputStream

@Suppress("DEPRECATION")
class RegistroFotoVetActivity : AppCompatActivity() {


    private lateinit var binding:ActivityRegistroFotoVetBinding
    private lateinit var file: File

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroFotoVetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFoto.setOnClickListener {
            verificarPermisoCamara()
        }

        binding.btnGaleria.setOnClickListener {
            selectImageFromGallery()
        }

        binding.btnContinuar.setOnClickListener {
            Toast .makeText(this, "Registro exitoso", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MapaLocalizaciones::class.java)
            startActivity(intent)
        }
    }
}

private fun subirImagenFirebase(uri: Uri, callback: (Boolean) -> Unit) {
    val firebaseStorage = FirebaseStorage.getInstance()
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()

    val storageRef = firebaseStorage.reference.child("Imagenes").child("${System.currentTimeMillis()}.jpg")

    storageRef.putFile(uri).addOnSuccessListener { uploadTask ->
        uploadTask.storage.downloadUrl.addOnSuccessListener { downloadUri ->
            val usuario = Usuario()
            usuario.foto = downloadUri.toString()

            val updateData = HashMap<String, Any>()
            updateData["foto"] = usuario.foto!!

            firebaseDatabase.reference.child("usuarios").child(firebaseAuth.currentUser!!.uid).updateChildren(updateData)
                .addOnCompleteListener {
                    callback(true) // Subida exitosa, llamada al callback con true
                }
                .addOnFailureListener {
                    callback(false) // Error en la subida, llamada al callback con false
                }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
private fun guardarEnGaleria() {
    val contenedor = crearContenedor()
    val uri = guardarImagen(contenedor)
    limpiarContenedor(contenedor, uri)
    Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_LONG).show()
}


@RequiresApi(Build.VERSION_CODES.Q)
private fun crearContenedor(): ContentValues {
    val nombreArchivo = file.name
    val tipoArchivo = "image/jpeg"
    return ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
        put(MediaStore.Files.FileColumns.MIME_TYPE, tipoArchivo)
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }
}

private fun guardarImagen(contenedor: ContentValues): Uri? {
    val resolver = contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contenedor)
    uri?.let {
        val stream: OutputStream? = resolver.openOutputStream(uri)
        stream?.let {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val rotatedBitmap = rotarImagen(bitmap)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()
        }
    }
    return uri
}
private fun rotarImagen(bitmap: Bitmap): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(90f)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun limpiarContenedor(contenedor: ContentValues, uri: Uri?) {
    contenedor.clear()
    contenedor.put(MediaStore.MediaColumns.IS_PENDING, 0)
    uri?.let {
        contentResolver.update(it, contenedor, null, null)
    }
}


// Tomar una foto con la cámara

private fun verificarPermisoCamara() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        tomarFoto()
    } else {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
    }
}

private fun tomarFoto() {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    file = crearArchivo()
    val uri = FileProvider.getUriForFile(this, "com.example.taller3.fileprovider", file)
    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
    startActivityForResult(intent, 2)
}

private fun crearArchivo(): File {
    val nombreArchivo = "${System.currentTimeMillis()}.jpg"
    val directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(nombreArchivo, null, directorio)
}

// Seleccionar una foto de la galería

private fun selectImageFromGallery() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, 3)
}
