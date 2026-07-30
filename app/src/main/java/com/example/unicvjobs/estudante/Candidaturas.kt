package com.example.unicvjobs.estudante

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.classes.Candidatura
import com.example.unicvjobs.databinding.ActivityCandidaturasBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class Candidaturas : AppCompatActivity() {
    private lateinit var menu: ActivityCandidaturasBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private var todasCandidaturas = mutableListOf<Candidatura>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityCandidaturasBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val userId = auth.currentUser?.uid

        if (userId != null) {
            carregarDados(userId)
        }

        configurarNavegacao()
        configurarFiltros()
    }

    private fun carregarDados(userId: String) {
        db.collection("candidaturas")
            .whereEqualTo("idAluno", userId)
            .get()
            .addOnSuccessListener { documents ->
                todasCandidaturas.clear()

                for (doc in documents) {
                    val c = doc.toObject(Candidatura::class.java)
                    todasCandidaturas.add(c)
                }

                atualizarInterface(todasCandidaturas)
                atualizarCardsResumo(todasCandidaturas)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarInterface(lista: List<Candidatura>) {
        if (lista.isEmpty()) {
            menu.emptyStateView.visibility = View.VISIBLE
            menu.listaCandidatura.visibility = View.GONE
        } else {
            menu.emptyStateView.visibility = View.GONE
            menu.listaCandidatura.visibility = View.VISIBLE

            // Mapeando para exibição simples (Melhorar criando um Adapter Customizado depois)
            val nomesVagas = lista.map { "${it.nome}\nStatus: ${it.status}" }
            val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, nomesVagas)
            menu.listaCandidatura.adapter = adapter
        }
    }

    private fun atualizarCardsResumo(lista: List<Candidatura>) {
        val total = lista.size
        val aprovados = lista.count { it.status == "Aprovado" }
        val pendentes = lista.count { it.status == "Pendente" }

        menu.totalCandidatura.text = total.toString()
        menu.candidaturaAtivas.text = aprovados.toString()
        menu.candidaturasPendentes.text = pendentes.toString()
    }

    private fun configurarFiltros() {
        menu.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                // Ajuste os nomes de acordo com o texto dos seus chips no XML
                group.getChildAt(1).id -> "Aprovado"
                group.getChildAt(2).id -> "Pendente"
                else -> "Todas"
            }

            if (filter == "Todas") {
                atualizarInterface(todasCandidaturas)
            } else {
                val listaFiltrada = todasCandidaturas.filter { it.status == filter }
                atualizarInterface(listaFiltrada)
            }
        }
    }

    private fun configurarNavegacao() {
        menu.navBarAluno.selectedItemId = com.example.unicvjobs.R.id.candidaturas

        menu.navBarAluno.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.unicvjobs.R.id.vagas -> {
                    startActivity(Intent(this, MainActivityAluno::class.java))
                    finish()
                    true
                }
                com.example.unicvjobs.R.id.perfil -> {
                    startActivity(Intent(this, PerfilAluno::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}