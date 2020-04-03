package com.jujulad.skynetapp.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jujulad.skynetapp.MainActivity
import com.jujulad.skynetapp.R

class SigninActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "Sign"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val username = findViewById<EditText>(R.id.txt_username_sign)
        val pass = findViewById<EditText>(R.id.txt_password_sign)
        val pass2 = findViewById<EditText>(R.id.txt_password2)
        val warning = findViewById<TextView>(R.id.txt_waring)
        val sign_btn = findViewById<Button>(R.id.sign_btn)
        warning.visibility = View.INVISIBLE
        sign_btn.setOnClickListener {
            if (checkData(username.text.toString(), pass.text.toString(), pass2.text.toString())) {
                db.collection("users").get()
                    .addOnSuccessListener { docs ->
                        val up = docs.map { it.id to it.data["password"] as String }.toMap()
                        val key = username.text.toString()
                        val password = Encrypt.encrypt(pass.text.toString())

                        if (up.containsKey(key) && up[key] == password) warning.visibility = View.VISIBLE
                        else {
                            if (password != null) {
                                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("user", key).apply()
                                db.collection("users").document(key).set(
                                    mapOf(
                                        "flights" to mapOf<String, String>(),
                                        "password" to password
                                    )
                                ).addOnSuccessListener {
                                    Log.d(TAG, "Success!")
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }.addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
                            }
                        }
                    }.addOnFailureListener { e -> Log.w("XXX", "Error writing document", e) }
            } else warning.visibility = View.VISIBLE

        }
    }


    private fun checkData(username: String, password: String, password2: String): Boolean {
        if (username == "") return false
        if (username.length < 4) return false
        if (password == "") return false
        if (password.length < 4) return false
        if (password2 != password) return false
        return true
    }


}