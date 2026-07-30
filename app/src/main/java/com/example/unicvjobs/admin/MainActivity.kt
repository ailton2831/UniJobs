package com.example.unicvjobs.admin

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.autenticacao.TelaAutenticacao
import com.example.unicvjobs.classes.Empresa
import com.example.unicvjobs.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var menu: ActivityMainBinding

    private val listaEmpresas = mutableListOf<Empresa>()
    private val dadosExibicao = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityMainBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore

        adapter = ArrayAdapter(this, R.layout.simple_list_item_1, dadosExibicao)
        menu.listaEmpresaPendente.adapter = adapter

        // Carregar Empresas Pendentes
        db.collection("users")
            .whereEqualTo("tipo", "Empresa")
            .whereEqualTo("status", false) // Usando "status" para bater com o login
            .get()
            .addOnSuccessListener { documents ->
                listaEmpresas.clear()
                dadosExibicao.clear()

                for (document in documents) {
                    val empresa = document.toObject(Empresa::class.java)
                    empresa.idDocumento = document.id
                    listaEmpresas.add(empresa)
                    dadosExibicao.add("${empresa.nome} - ${empresa.area}\n${empresa.localizacao}")
                }

                adapter.notifyDataSetChanged()
                menu.emptyStateView.visibility = if (listaEmpresas.isEmpty()) View.VISIBLE else View.GONE
            }

        // Clique na lista
        menu.listaEmpresaPendente.setOnItemClickListener { _, _, position, _ ->
            val empresa = listaEmpresas[position]

            AlertDialog.Builder(this)
                .setTitle("Análise de Registro")
                .setMessage("Empresa: ${empresa.nome}\nO que deseja fazer?")
                .setPositiveButton("Aprovar") { _, _ ->

                    db.collection("users").document(empresa.idDocumento)
                        .update("status", true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Empresa aprovada!", Toast.LENGTH_SHORT).show()
                            listaEmpresas.removeAt(position)
                            dadosExibicao.removeAt(position)
                            adapter.notifyDataSetChanged()
                        }
                }
                .setNegativeButton("Rejeitar", null)

                .show()
        }

        // Configuração da NavBar
        menu.navBarAdmin.selectedItemId = com.example.unicvjobs.R.id.empresas
        menu.navBarAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.unicvjobs.R.id.listas -> {
                    startActivity(Intent(this, Listas::class.java))
                    finish()
                    true
                }
                com.example.unicvjobs.R.id.dashboard -> {
                    startActivity(Intent(this, Dashboard::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        menu.logout.setOnClickListener { logout() }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, TelaAutenticacao::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}