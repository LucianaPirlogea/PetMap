package com.example.petmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val email = intent.getStringExtra("email")
        val displayName = intent.getStringExtra("name")

        findViewById<TextView>(R.id.textView).text = email + "\n" + displayName

        findViewById<Button>(R.id.signOutBtn).setOnClickListener {
            clearGoogleSignInCache()
            startActivity(Intent(this , SignInActivity::class.java))
        }
    }

    private fun clearGoogleSignInCache() {
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            .addOnCompleteListener {
                Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
            }
    }
}