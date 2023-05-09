package com.example.taller3.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taller3.MapaUsuario
import com.example.taller3.R
import com.example.taller3.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@Suppress("DEPRECATION")
class UsuarioAdapter(private var usuarios: List<Usuario>):RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>(),Parcelable {

    private var mapIntent: Intent = Intent()

    constructor(parcel: Parcel) : this(parcel.createTypedArrayList(Usuario)!!) {
        mapIntent = parcel.readParcelable(Intent::class.java.classLoader)!!
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.bind(usuario)
    }

    override fun getItemCount(): Int {
        return usuarios.size
    }


    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nombreTextView: TextView = itemView.findViewById(R.id.tv_NombreUsuario)
        private val fotoImageView: ImageView = itemView.findViewById(R.id.imageViewUsuario)
        private val btn_rastrear: Button = itemView.findViewById(R.id.btn_rastrearUsuario)


        @SuppressLint("SetTextI18n")
        fun bind(usuario: Usuario) {
            val nombreUsuario = usuario.nombre
            val nombreCompleto = usuario.nombre + " " + usuario.apellido

            nombreTextView.text = nombreCompleto
            Glide.with(itemView.context).load(usuario.foto).into(fotoImageView)

            btn_rastrear.setOnClickListener {
                if (nombreUsuario != null) {
                    buscarUsuario(nombreUsuario) { latitud, longitud ->
                        if (latitud != null && longitud != null) {
                            mapIntent = Intent(itemView.context, MapaUsuario::class.java)
                            mapIntent.putExtra("latitudUsuarioRastrear", latitud)
                            mapIntent.putExtra("longitudUsuarioRastrear", longitud)
                            mapIntent.putExtra("nombreUsuarioRastrear", nombreCompleto)
                            itemView.context.startActivity(mapIntent)
                        } else {
                            Toast.makeText(itemView.context, "No se pudo rastrear al usuario ", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun buscarUsuario(nombre: String, callback: (Double?, Double?) -> Unit) {
        val mRootReference = FirebaseDatabase.getInstance().reference.child("usuarios")
        val query = mRootReference.orderByChild("nombre").equalTo(nombre)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val latitudString = snapshot.child("latitud").getValue(String::class.java)
                    val longitudString = snapshot.child("longitud").getValue(String::class.java)

                    val latitud = latitudString?.replace(",", ".")?.toDoubleOrNull()
                    val longitud = longitudString?.replace(",", ".")?.toDoubleOrNull()

                    if (latitud != null && longitud != null) {
                        callback(latitud, longitud)
                    } else {
                        callback(null, null)
                    }
                    break
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(null, null)
            }
        })
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(mapIntent, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UsuarioAdapter> {
        override fun createFromParcel(parcel: Parcel): UsuarioAdapter {
            return UsuarioAdapter(parcel)
        }

        override fun newArray(size: Int): Array<UsuarioAdapter?> {
            return arrayOfNulls(size)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarUsuarios(usuarios: List<Usuario>) {
        this.usuarios = usuarios.filter { it.estado == "Disponible" }
        notifyDataSetChanged()
    }
}