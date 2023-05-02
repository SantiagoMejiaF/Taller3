package com.example.taller3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3.databinding.MapaLocalizacionesBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class MapaLocalizaciones : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: MapaLocalizacionesBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Se infla el layout de la actividad
        binding = MapaLocalizacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configurar opciones de mapa
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        // Agregar marcadores de puntos de interés
        val jsonString =
            applicationContext.assets.open("locations.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val locations = jsonObject.getJSONArray("locationsArray")

        // Agregar marcadores para cada localización de punto de interés
        for (i in 0 until locations.length()) {
            val location = locations.getJSONObject(i)
            val name = location.getString("name")
            val lat = location.getDouble("latitude")
            val lng = location.getDouble("longitude")
            val latLng = LatLng(lat, lng)
            mMap.addMarker(MarkerOptions().position(latLng).title(name))
        }

        // Agregar marcador de ubicación actual
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("Ubicación actual"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
                } ?: run {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Menu de opciones
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_disconnect -> {
                // Lógica para cerrar sesión
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, Login::class.java)
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_LONG).show()
                startActivity(intent)
                finish()
                return true
            }
            R.id.menu_available -> {
                // Lógica para establecer estado como disponible
                val currentUser = FirebaseAuth.getInstance().currentUser
                val usuario = Usuario()
                usuario.setUid(currentUser!!.uid)
                usuario.actualizarEstado(this,"Disponible")
                Toast.makeText(this, "Estado actualizado", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.menu_unavailable -> {
                // Lógica para establecer estado como desconectado
                val currentUser = FirebaseAuth.getInstance().currentUser
                val usuario = Usuario()
                usuario.setUid(currentUser!!.uid)
                usuario.actualizarEstado(this,"No disponible")
                Toast.makeText(this, "Estado actualizado", Toast.LENGTH_LONG).show()
                return true
            }

            R.id.btn_lista_usuarios -> {
                // Lógica para ir a la lista de usuarios
                val intent = Intent(this, ListaUsuarios::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this,"Permiso de ubicación denegado",Toast.LENGTH_LONG).show()
            }
        }
    }
}