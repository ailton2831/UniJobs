package com.example.unicvjobs.vaga

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.databinding.ActivityItemVagaBinding
import com.google.firebase.auth.FirebaseAuth

class ItemVaga : AppCompatActivity() {

    private lateinit var menu: ActivityItemVagaBinding

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityItemVagaBinding.inflate(layoutInflater)
        setContentView(menu.root)


    }
}