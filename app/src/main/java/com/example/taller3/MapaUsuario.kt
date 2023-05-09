package com.example.taller3

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapaUsuario : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapa: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitudRastrear: Double = 0.0
    private var longitudRastrear: Double = 0.0
    private var nombreUsuarioRastrear: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapa_usuario)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_usuarios) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Obtener la latitud y longitud del intent
        latitudRastrear = intent.getDoubleExtra("latitudUsuarioRastrear", 0.0)
        longitudRastrear = intent.getDoubleExtra("longitudUsuarioRastrear", 0.0)
        nombreUsuarioRastrear = intent.getStringExtra("nombreUsuarioRastrear")
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mapa = googleMap
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar opciones de mapa
        mapa.uiSettings.isZoomGesturesEnabled = true
        mapa.uiSettings.isZoomControlsEnabled = true

        // Configurar la posición del mapa con la latitud y longitud
        val ubicacionUsuarioSeleccionado = LatLng(latitudRastrear, longitudRastrear)

        // Agregar un marcador en la ubicación
        val marcadorUsuarioRastreado = MarkerOptions()
            .position(ubicacionUsuarioSeleccionado)
            .title(nombreUsuarioRastrear)

        mapa.addMarker(marcadorUsuarioRastreado)

        // Obtener la ubicación actual del usuario y mostrarla en el mapa
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val ubicacionUsuarioActual = LatLng(location.latitude, location.longitude)
                    mapa.addMarker(MarkerOptions().position(ubicacionUsuarioActual).title("Ubicación actual"))

                    // Agregar una línea recta entre los dos marcadores
                    val builder = LatLngBounds.Builder()
                    builder.include(ubicacionUsuarioSeleccionado)
                    builder.include(ubicacionUsuarioActual)
                    val bounds = builder.build()
                    val padding = 10
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    mapa.moveCamera(cameraUpdate)
                    agregarLineaRectaEntreMarcadores(ubicacionUsuarioActual, ubicacionUsuarioSeleccionado)
                } ?: run {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun agregarLineaRectaEntreMarcadores(ubicacionActual: LatLng, ubicacionRastrear: LatLng) {
        // Agregar una línea recta entre los dos marcadores
        mapa.addPolyline(
            PolylineOptions()
                .add(ubicacionActual, ubicacionRastrear)
                .color(Color.RED)
        )

        // Calcular la distancia entre las dos ubicaciones
        val distancia = calcularDistancia(ubicacionActual, ubicacionRastrear)

        // Mostrar la distancia en el mapa
        val distanciaText = String.format("%.2f km", distancia)
        val distanciaMarker = MarkerOptions()
            .position(obtenerPuntoMedio(ubicacionActual, ubicacionRastrear))
            .title(distanciaText)
        mapa.addMarker(distanciaMarker)
    }

    private fun calcularDistancia(ubicacion1: LatLng, ubicacion2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            ubicacion1.latitude, ubicacion1.longitude,
            ubicacion2.latitude, ubicacion2.longitude,
            results
        )
        return results[0] / 1000  // Convertir a kilómetros
    }

    private fun obtenerPuntoMedio(ubicacion1: LatLng, ubicacion2: LatLng): LatLng {
        val latitudMedia = (ubicacion1.latitude + ubicacion2.latitude) / 2
        val longitudMedia = (ubicacion1.longitude + ubicacion2.longitude) / 2
        return LatLng(latitudMedia, longitudMedia)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Llamar a onMapReady() nuevamente para mostrar ambos marcadores después de obtener los permisos
                onMapReady(mapa)
            } else {
                Toast.makeText(this,"Permiso de ubicación denegado",Toast.LENGTH_LONG).show()
            }
        }
    }
}