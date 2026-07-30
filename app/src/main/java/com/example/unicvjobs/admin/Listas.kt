package com.example.unicvjobs.admin

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.admin.MainActivity
import com.example.unicvjobs.classes.User
import com.example.unicvjobs.databinding.ActivityListasBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class Listas : AppCompatActivity() {
    private lateinit var menu: ActivityListasBinding
    private lateinit var auth: FirebaseAuth
    private val listaUserTotal = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityListasBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore

        val itenstipo = listOf("Todos", "Estudante", "Empresa")
        val adaptertipo = ArrayAdapter(this, R.layout.simple_spinner_item, itenstipo)
        adaptertipo.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerTipo.adapter = adaptertipo


        db.collection("users").get().addOnSuccessListener { documents ->
            listaUserTotal.clear()
            for (document in documents) {
                val user = document.toObject(User::class.java)
                listaUserTotal.add(user)
            }

            atualizarInterface(listaUserTotal)
        }

        // 3. Botão de Filtro Único
        menu.buttonFiltro.setOnClickListener {
            val selecionado = menu.spinnerTipo.selectedItem.toString()

            val listaFiltrada = if (selecionado == "Todos") {
                listaUserTotal
            } else {
                listaUserTotal.filter { it.tipo == selecionado }
            }

            atualizarInterface(listaFiltrada)
        }

        val nav = menu.navBarAdmin

        nav.selectedItemId = com.example.unicvjobs.R.id.listas

        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.unicvjobs.R.id.dashboard -> {
                    startActivity(Intent(this, Dashboard::class.java))
                    finish(); true
                }
                com.example.unicvjobs.R.id.empresas -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish(); true
                }
                else -> false
            }
        }
    }

    // Função auxiliar para não repetir código
    fun atualizarInterface(lista: List<User>) {
        if (lista.isEmpty()) {
            menu.emptyStateView.visibility = View.VISIBLE
            menu.listaUsers.visibility = View.GONE
            menu.textResultCount.text = "0 usuários"
        } else {
            menu.emptyStateView.visibility = View.GONE
            menu.listaUsers.visibility = View.VISIBLE
            menu.textResultCount.text = "${lista.size} usuários encontrados"

            val dadosExibicao = lista.map { "${it.nome}\n${it.email} (${it.tipo})" }
            val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, dadosExibicao)
            menu.listaUsers.adapter = adapter
        }
    }
}