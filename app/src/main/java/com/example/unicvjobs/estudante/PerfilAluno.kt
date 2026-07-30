package com.example.unicvjobs.estudante

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.R
import com.example.unicvjobs.databinding.ActivityPerfilAlunoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class PerfilAluno : AppCompatActivity() {

    private lateinit var menu: ActivityPerfilAlunoBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        menu = ActivityPerfilAlunoBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore
        val userId = auth.currentUser?.uid

        if (userId != null) {

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Extração de dados com valores padrão para evitar campos vazios
                        val nome = document.getString("nome") ?: "Nome não disponível"
                        val email = document.getString("email") ?: "Email não disponível"
                        val curso = document.getString("curso") ?: "Não informado"
                        val universidade = document.getString("universidade") ?: "Não informado"
                        val grau = document.getString("grau") ?: "Não informado"

                        // Ligação com os IDs do seu XML
                        menu.nomeUtilizador.text = nome
                        menu.emailPerfil.text = email
                        menu.cursoPerfil.text = curso
                        menu.universidadePerfil.text = universidade
                        menu.grauPerfil.text = grau


                        if (nome.isNotEmpty()) {
                            menu.inicial.text = nome.take(2).uppercase()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                }
        }


        menu.botonEditaRPerfil.setOnClickListener {
            val intent = Intent(this, SetupEstudante::class.java)
            startActivity(intent)
        }

        menu.navBarAluno.selectedItemId = R.id.perfil

        menu.navBarAluno.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.vagas -> {
                    startActivity(Intent(this, MainActivityAluno::class.java))
                    finish()
                    true
                }
                R.id.candidaturas -> {
                    startActivity(Intent(this, Candidaturas::class.java))
                    finish()
                    true
                }
                R.id.perfil -> true
                else -> false
            }
        }
    }
}