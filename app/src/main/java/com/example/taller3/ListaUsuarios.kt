package com.example.taller3

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.database.*
import android.os.Bundle
import android.service.controls.ControlsProviderService.TAG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide


class ListaUsuarios : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserListAdapter
    private var userList: MutableList<Usuario> = mutableListOf()
    private lateinit var myRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_usuarios)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserListAdapter(userList, this)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        userList = mutableListOf()
        loadUsers()
    }

    private fun loadUsers() {
        myRef = database.getReference(PATH_USERS)
        myRef.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.R)
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear() // Limpiamos la lista antes de añadir los nuevos usuarios
                for (singleSnapshot in dataSnapshot.children) {
                    val myUser = singleSnapshot.getValue(Usuario::class.java)
                    Log.i(TAG, "Encontró usuario: " + myUser?.getNombre())
                    if (myUser != null) {
                        userList.add(myUser)
                        downloadImage(myUser)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            @RequiresApi(Build.VERSION_CODES.R)
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "error en la consulta", databaseError.toException())
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun downloadImage(user: Usuario) {
        val storageRef = storage.reference.child("images/${user.getUid()}.jpg")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            user.setImagenUri(uri)
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            // error al descargar la imagen
        }
    }
}

class UserListAdapter(private val userList: List<Usuario>, private val context: Context) :
    RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.nameTextView.text = user.getNombre()
        Glide.with(context).load(user.getImagenUri()).into(holder.imageView)
        holder.locationButton.setOnClickListener {
            // handle location button click
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val locationButton: Button = itemView.findViewById(R.id.locationButton)
    }
}

