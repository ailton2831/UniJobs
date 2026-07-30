package com.example.unicvjobs.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.admin.Listas
import com.example.unicvjobs.admin.MainActivity
import com.example.unicvjobs.R
import com.example.unicvjobs.databinding.ActivityDashboardBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class Dashboard : AppCompatActivity() {
    private lateinit var menu: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth

        // Chamamos a função para carregar os dados reais do banco
        carregarEstatisticas()

        // Navegação

        val nav = menu.navBarAdmin

        nav.selectedItemId = R.id.dashboard

        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.listas -> {
                    startActivity(Intent(this, Listas::class.java))
                    finish()
                    true
                }
                R.id.empresas -> {
                    // Cuidado aqui: MainActivity costuma ser o Login ou Aluno.
                    // Garante que é a tela certa para o Admin.
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun carregarEstatisticas() {
        // 1. Contar Todas as Empresas
        db.collection("users").whereEqualTo("tipo", "Empresa").get()
            .addOnSuccessListener { menu.totalempresa.text = it.size().toString() }
            .addOnFailureListener { e -> println("Erro Dash: ${e.message}") }

        // 2. Contar Estudantes
        db.collection("users").whereEqualTo("tipo", "Estudante").get()
            .addOnSuccessListener { menu.totalestudante.text = it.size().toString() }

        // 3. Contar Vagas
        db.collection("vagas").get()
            .addOnSuccessListener { menu.totalvagas.text = it.size().toString() }

        // 4. Contar Candidaturas
        db.collection("candidaturas").get()
            .addOnSuccessListener { candidaturas ->
                val total = candidaturas.size()
                menu.totalcandidatura.text = total.toString()

                if (total > 0) {
                    val aprovadas = candidaturas.count { it.getString("status") == "Aprovado" }
                    val taxa = (aprovadas.toDouble() / total.toDouble()) * 100
                    menu.taxaSucesso.text = String.format("%.1f%%", taxa)
                } else {
                    menu.taxaSucesso.text = "0%"
                }
            }

        // 5. Contar Empresas já APROVADAS
        db.collection("users")
            .whereEqualTo("tipo", "Empresa")
            .whereEqualTo("status", true) // Use o Boolean true, não a String "aprovada"
            .get()
            .addOnSuccessListener { menu.totalempresaA.text = it.size().toString() }
    }
}