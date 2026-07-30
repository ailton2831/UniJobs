package com.example.unicvjobs.empresa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.autenticacao.TelaAutenticacao
import com.example.unicvjobs.databinding.ActivityTelaEsperaBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class TelaEspera : AppCompatActivity() {

    private lateinit var menu: ActivityTelaEsperaBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityTelaEsperaBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore

        menu.bttnStatus.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.getBoolean("status") == true) {
                        startActivity(Intent(this, MainActivityEmpresa::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Ainda em análise...", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        fun Logout() {

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, TelaAutenticacao::class.java)


            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }

        menu.bttnLogout.setOnClickListener {
            Logout()
        }

    }
}