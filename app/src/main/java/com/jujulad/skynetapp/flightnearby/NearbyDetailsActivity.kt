package com.jujulad.skynetapp.flightnearby

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jujulad.skynetapp.R

class NearbyDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_details)
        val text = findViewById<TextView>(R.id.txt_details)
        val info = intent.extras?.getString("info") ?: "No info availabale"
        text.text = info
        text.movementMethod = ScrollingMovementMethod()
    }
}