package com.example.petmap

import MyPetsFragment
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.petmap.models.Profile
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    lateinit var bottomNav : BottomNavigationView
    private val storageRef = Firebase.storage.reference
    private val gson = Gson()
    private var account: Profile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val initialFragment = intent.getStringExtra("initial_fragment")
        loadFragment(initialFragment)
        downloadAccountData()

        bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.add -> {
                    val intent : Intent = Intent(this , MapsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.account -> {
                    val intent : Intent = Intent(this , ProfileActivity::class.java)
                    intent.putExtra("email" , account!!.email)
                    intent.putExtra("name" , account!!.name)
                    startActivity(intent)
                    true
                }
                R.id.home -> {
                    val intent : Intent = Intent(this , MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    true
                }
            }
        }
    }

    private fun loadFragment(initialFragment: String?) {
        val fragment = when (initialFragment) {
            "MyPetsFragment" -> MyPetsFragment()
            // Other fragments could be added here
            else -> MyPetsFragment() // Default to MyPetsFragment
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun downloadAccountData() {
        val accountRef = storageRef.child("account.json")
        accountRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val json = String(bytes, Charsets.UTF_8)
            account = gson.fromJson(json, Profile::class.java)
        }.addOnFailureListener { exception ->
            // Handle error
            Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
        }
    }
}
