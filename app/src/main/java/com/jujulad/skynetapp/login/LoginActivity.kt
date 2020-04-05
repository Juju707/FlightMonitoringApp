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
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

//Aktywność pozwalająca na logowanie się istniejącego użytkownika.
class LoginActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginBtn = findViewById<Button>(R.id.btn_login)
        val signinBtn = findViewById<Button>(R.id.btn_signin)
        val username = findViewById<EditText>(R.id.txt_username_sign)
        val password = findViewById<EditText>(R.id.txt_password_sign)
        val alert = findViewById<TextView>(R.id.txt_waring)
        alert.visibility = View.INVISIBLE
        //Po kliknięciu przycisku sprawdza poprawność danych.
        loginBtn.setOnClickListener {
            if (checkData(username.text.toString(), password.text.toString())) {
                //pobiera listę użytkowników z bazy danych
                db.collection("users").get()
                    .addOnSuccessListener { docs ->
                        val up = docs.map { it.id to it.data["password"] as String }.toMap()
                        val key = username.text.toString()
                        val pass = Encrypt.encrypt(password.text.toString())
                        if (up.containsKey(key) && up[key] == pass) {
                            //Jeśli poprawnie zalogowane zapisuje użytkownika i przechodzi do głownej aktywności.
                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("user", key).apply()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else alert.visibility = View.VISIBLE

                    }.addOnFailureListener { e -> Log.w("XXX", "Error writing document", e) }
            } else alert.visibility = View.VISIBLE

        }

        //Otwarcie ekranu tworzenia użytkownika.
        signinBtn.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Sprawdzenie poprawności danych.
    private fun checkData(username: String, password: String): Boolean {
        if (username == "") return false
        if (username.length < 4) return false
        if (password == "") return false
        if (password.length < 4) return false
        return true
    }


}

class Encrypt {

    companion object {
        fun encrypt(text: String): String? {
            try {
                val md = MessageDigest.getInstance("MD5");
                md.update(text.toByteArray())
                val bytes = md.digest()
                val sb = StringBuilder()
                for (i in bytes.indices) {
                    sb.append(Integer.toString((bytes[i] and 0xFF.toByte()) + 0x100, 16).substring(1))
                }
                return sb.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return null
        }
    }

}
