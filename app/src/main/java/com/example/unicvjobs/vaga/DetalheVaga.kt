package com.example.unicvjobs.vaga

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unicvjobs.adapter.CandidaturaAdapter
import com.example.unicvjobs.classes.Candidatura
import com.example.unicvjobs.databinding.ActivityDetalheVagaBinding
import com.example.unicvjobs.estudante.Candidato
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class DetalheVaga : AppCompatActivity() {

    private lateinit var menu: ActivityDetalheVagaBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            menu = ActivityDetalheVagaBinding.inflate(layoutInflater)
            setContentView(menu.root)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro no XML da DetalheVaga: Verifique os IDs", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // --- ESCONDE TUDO que depende do tipo de usuário ANTES de qualquer consulta.
        menu.bttnCandidatar.visibility = View.GONE
        menu.botaoFecharVaga.visibility = View.GONE
        menu.txtCandidatura.visibility = View.GONE
        menu.RVcandidaturas.visibility = View.GONE

        auth = Firebase.auth
        val db = Firebase.firestore
        val user = auth.currentUser?.uid

        // 1. Recebendo ID
        val id = intent.getStringExtra("ID_VAGA")
        if (id.isNullOrEmpty()) {
            Toast.makeText(this, "Erro: Vaga não encontrada (ID Nulo)", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        menu.RVcandidaturas.layoutManager = LinearLayoutManager(this)

        var statusVaga: String? = null
        var vagaCarregada = false
        var tipoUsuarioCarregado = false
        var tipoUser: String? = null

        // UX entre aluno e empresa
        fun tentarAplicarUX() {
            if (!vagaCarregada || !tipoUsuarioCarregado) return

            when {
                tipoUser.equals("aluno", ignoreCase = true) || tipoUser.equals("Estudante", ignoreCase = true) -> {
                    // UX AlUNO
                    menu.bttnCandidatar.visibility = View.VISIBLE
                    if (statusVaga == "Fechado") {
                        menu.bttnCandidatar.visibility = View.GONE
                    } else {
                        user?.let { uid ->
                            db.collection("candidaturas")
                                .whereEqualTo("idVaga", id)
                                .whereEqualTo("idAluno", uid)
                                .get()
                                .addOnSuccessListener { query ->
                                    if (!query.isEmpty) {
                                        menu.bttnCandidatar.text = "Já Candidatado"
                                        menu.bttnCandidatar.isEnabled = false
                                        menu.bttnCandidatar.alpha = 0.6f
                                    }
                                }
                        }
                    }
                }
                tipoUser.equals("empresa", ignoreCase = true) -> {
                    // UX EMPRESA
                    menu.txtCandidatura.visibility = View.VISIBLE
                    menu.RVcandidaturas.visibility = View.VISIBLE
                    menu.botaoFecharVaga.visibility = if (statusVaga == "Fechado") View.GONE else View.VISIBLE
                    menu.botaoFecharVaga.isEnabled = statusVaga != "Fechado"
                    carregarCandidatos(db, menu, id)
                }
            }
        }

        // Carregar Detalhes da Vaga
        db.collection("vagas").document(id).get().addOnSuccessListener { result ->
            if (result.exists()) {
                try {
                    val titulo = result.getString("titulo") ?: "Sem título"
                    val tipo = result.getString("tipo")
                    val local = result.getString("localizacao")
                    val area = result.getString("area")
                    val descricao = result.getString("descricao")
                    val status = result.getString("status")
                    statusVaga = status

                    menu.status.text = status
                    menu.localVaga.text = local
                    menu.tipo.text = tipo
                    menu.area.text = area
                    menu.titulo.text = titulo
                    menu.descricao.text = descricao

                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao preencher tela: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            vagaCarregada = true
            tentarAplicarUX()
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao baixar vaga", Toast.LENGTH_SHORT).show()
            vagaCarregada = true
            tentarAplicarUX()
        }

        // Descobrir tipo de usuário (aluno/empresa)
        if (user != null) {
            db.collection("users").document(user).get().addOnSuccessListener { document ->
                tipoUser = document.getString("tipo")
                tipoUsuarioCarregado = true
                tentarAplicarUX()
            }.addOnFailureListener {
                tipoUsuarioCarregado = true
                tentarAplicarUX()
            }
        } else {
            tipoUsuarioCarregado = true
            tentarAplicarUX()
        }

        menu.botaoFecharVaga.setOnClickListener {
            db.collection("vagas").document(id).update("status", "Fechado")
                .addOnSuccessListener {
                    menu.status.text = "Fechado"
                    menu.botaoFecharVaga.visibility = View.GONE
                    Toast.makeText(this, "Vaga encerrada!", Toast.LENGTH_SHORT).show()
                }
        }

        menu.bttnCandidatar.setOnClickListener {
            val intent = Intent(this, Candidato::class.java)
            intent.putExtra("ID_VAGA", id)
            startActivity(intent)
        }
    }

    // Função para carregar candidatos
    private fun carregarCandidatos(
        db: com.google.firebase.firestore.FirebaseFirestore,
        menu: ActivityDetalheVagaBinding,
        idVaga: String
    ) {
        db.collection("candidaturas")
            .whereEqualTo("idVaga", idVaga)
            .get()
            .addOnSuccessListener { documentos ->
                try {
                    val lista = mutableListOf<Candidatura>()
                    for (doc in documentos) {
                        val c = doc.toObject(Candidatura::class.java)
                        c.idCandidatura = doc.id
                        lista.add(c)
                    }
                    menu.RVcandidaturas.adapter = CandidaturaAdapter(lista)
                } catch (e: Exception) {
                    Toast.makeText(menu.root.context, "Erro na lista de candidatos: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}