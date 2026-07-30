package com.example.unicvjobs.estudante

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unicvjobs.R
import com.example.unicvjobs.autenticacao.TelaAutenticacao
import com.example.unicvjobs.classes.Vaga
import com.example.unicvjobs.adapter.VagasAdapter
import com.example.unicvjobs.databinding.ActivityMainAlunoBinding
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivityAluno : AppCompatActivity() {
    private lateinit var menu: ActivityMainAlunoBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val listaOriginal = mutableListOf<Vaga>()

    private lateinit var vagasAdapter: VagasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityMainAlunoBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth

        // Inicializa o adapter com lista vazia
        vagasAdapter = VagasAdapter(listaOriginal)
        menu.vagas.layoutManager = LinearLayoutManager(this)
        menu.vagas.adapter = vagasAdapter

        carregarVagas()

        // filtro
        menu.chipFilterGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chipId = checkedIds.firstOrNull()

            val listaFiltrada = if (chipId != null) {
                val textoFiltro = findViewById<Chip>(chipId).text.toString()
                if (textoFiltro == "Todas") listaOriginal
                else listaOriginal.filter { it.tipo.equals(textoFiltro, ignoreCase = true) }
            } else {
                listaOriginal
            }
            atualizarInterface(listaFiltrada)
        }

        menu.btnClearFilters.setOnClickListener {
            menu.chipFilterGroup.clearCheck()
            atualizarInterface(listaOriginal)
        }

        // Navegação
        menu.navBarAluno.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.perfil -> { startActivity(Intent(this, PerfilAluno::class.java)); true }
                R.id.candidaturas -> { startActivity(Intent(this, Candidaturas::class.java)); true }
                else -> false
            }
        }

        menu.logout.setOnClickListener { logout() }
    }

    private fun carregarVagas() {
        db.collection("vagas").get().addOnSuccessListener { documents ->
            listaOriginal.clear()
            for (document in documents) {
                val vaga = document.toObject(Vaga::class.java)
                vaga.idVaga = document.id
                listaOriginal.add(vaga)
            }
            atualizarInterface(listaOriginal)
        }
    }

    private fun atualizarInterface(lista: List<Vaga>) {
        if (lista.isEmpty()) {
            menu.emptyStateView.visibility = View.VISIBLE
            menu.vagas.visibility = View.GONE
        } else {
            menu.emptyStateView.visibility = View.GONE
            menu.vagas.visibility = View.VISIBLE
            vagasAdapter.atualizarLista(lista)
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, TelaAutenticacao::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}