package com.example.unicvjobs.empresa

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.R
import com.example.unicvjobs.databinding.ActivityPerfilEmpresaBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class PerfilEmpresa : AppCompatActivity() {

    private lateinit var menu: ActivityPerfilEmpresaBinding
    private lateinit var auth: FirebaseAuth
    private var editando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityPerfilEmpresaBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore
        val userId = auth.currentUser?.uid

        // 1. Carregar dados iniciais
        userId?.let { id ->
            db.collection("users").document(id).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val nome = document.getString("nome") ?: ""
                    val email = document.getString("email") ?: ""
                    val local = document.getString("localizacao") ?: ""
                    val area = document.getString("area") ?: ""
                    val desc = document.getString("descricao") ?: ""

                    menu.nomeEmpresa.setText(nome)
                    menu.areaEmpresa.setText(area)
                    menu.emailEmpresa.setText(email)
                    menu.localEmpresa.setText(local)
                    menu.descricao.setText(desc)

                    // foto perfil
                    if (nome.isNotEmpty()) {
                        menu.textCompanyInitials.text = nome.take(2).uppercase()
                    }
                }
            }
        }


        fun configurarModoEdicao(ativo: Boolean) {
            menu.nomeEmpresa.isEnabled = ativo
            menu.areaEmpresa.isEnabled = ativo
            menu.emailEmpresa.isEnabled = ativo
            menu.localEmpresa.isEnabled = ativo
            menu.descricao.isEnabled = ativo

            if (ativo) {
                menu.btnEditMode.text = "Guardar Alterações"
                menu.btnSaveProfile.visibility = View.VISIBLE
            } else {
                menu.btnEditMode.text = "Editar Perfil"
                menu.btnSaveProfile.visibility = View.GONE
            }
        }

        // 3. Função para salvar no Firestore
        fun salvarDados() {
            val userIdAtual = auth.currentUser?.uid ?: return

            val dadosEmpresa = mapOf(
                "nome" to menu.nomeEmpresa.text.toString(),
                "email" to menu.emailEmpresa.text.toString(),
                "localizacao" to menu.localEmpresa.text.toString(),
                "area" to menu.areaEmpresa.text.toString(),
                "descricao" to menu.descricao.text.toString()
            )

            db.collection("users").document(userIdAtual).update(dadosEmpresa)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    //
                    menu.textCompanyInitials.text = menu.nomeEmpresa.text.toString().take(2).uppercase()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao atualizar perfil", Toast.LENGTH_SHORT).show()
                }
        }

        configurarModoEdicao(false)

        // 4. Clique no botão Editar/Guardar
        menu.btnEditMode.setOnClickListener {
            if (!editando) {
                editando = true
                configurarModoEdicao(true)
            } else {
                salvarDados()
                editando = false
                configurarModoEdicao(false)
            }
        }

        // 5. Barra de Navegação
        menu.navBarEmpresa.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.criar_vagas -> {
                    startActivity(Intent(this, CriarVaga::class.java))
                    finish(); true
                }
                R.id.vagas -> {
                    startActivity(Intent(this, MainActivityEmpresa::class.java))
                    finish(); true
                }
                R.id.perfil -> true
                else -> false
            }
        }


        menu.navBarEmpresa.selectedItemId = R.id.perfil
    }
}