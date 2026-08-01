package com.example.unicvjobs.empresa

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
import com.example.unicvjobs.databinding.ActivityMainEmpresaBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivityEmpresa : AppCompatActivity() {

    private lateinit var menu: ActivityMainEmpresaBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onResume() {
        super.onResume()
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            carregarVagasDaEmpresa(userUid)
            carregarTotalCandidaturas(userUid)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityMainEmpresaBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val userUid = auth.currentUser?.uid ?: return

        menu.RVvagas.layoutManager = LinearLayoutManager(this)

        carregarVagasDaEmpresa(userUid)
        carregarTotalCandidaturas(userUid)

        menu.btnCreateFirst.setOnClickListener {
            startActivity(Intent(this, CriarVaga::class.java))
        }

        menu.navBarEmpresa.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.criar_vagas -> { startActivity(Intent(this, CriarVaga::class.java)); true }
                R.id.perfil -> { startActivity(Intent(this, PerfilEmpresa::class.java)); true }
                else -> false
            }
        }

        db.collection("users").document(userUid).get()
            .addOnSuccessListener { doc ->
                val aprovado = doc.getBoolean("status") ?: false
                if (!aprovado) {
                    startActivity(Intent(this, TelaEspera::class.java))
                    finish()
                }
            }

        menu.logout.setOnClickListener { logout() }
    }

    private fun carregarVagasDaEmpresa(empresaUid: String) {
        db.collection("vagas")
            .whereEqualTo("idEmpresa", empresaUid)
            .get()
            .addOnSuccessListener { result ->
                val listaVagas = result.map { doc ->
                    val vaga = doc.toObject(Vaga::class.java)
                    vaga.idVaga = doc.id
                    vaga
                }

                val ativas = listaVagas.count { it.status.equals("Aberta", true) || it.status.equals("Ativa", true) }

                menu.textTotalVagas.text = listaVagas.size.toString()
                menu.textVagasAtivas.text = ativas.toString()

                if (listaVagas.isEmpty()) {
                    menu.emptyStateView.visibility = View.VISIBLE
                    menu.RVvagas.visibility = View.GONE
                } else {
                    menu.emptyStateView.visibility = View.GONE
                    menu.RVvagas.visibility = View.VISIBLE
                    menu.RVvagas.adapter = VagasAdapter(listaVagas)
                }
            }
    }

    private fun carregarTotalCandidaturas(empresaUid: String) {
        db.collection("candidaturas")
            .whereEqualTo("idEmpresa", empresaUid)
            .get()
            .addOnSuccessListener { result ->
                menu.textCandidaturas.text = result.size().toString()
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