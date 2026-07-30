package com.example.unicvjobs.autenticacao

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.admin.MainActivity
import com.example.unicvjobs.databinding.ActivityTelaAutenticacaoBinding
import com.example.unicvjobs.empresa.MainActivityEmpresa
import com.example.unicvjobs.empresa.SetupEmpresa
import com.example.unicvjobs.estudante.MainActivityAluno
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class TelaAutenticacao : AppCompatActivity() {

    private lateinit var menu: ActivityTelaAutenticacaoBinding
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        // Verificar se o usuário já está autenticado ao iniciar a tela
        auth = FirebaseAuth.getInstance()
        val usuarioAtual = auth.currentUser

        if (usuarioAtual != null) {
            redirecionarUsuarioLogado()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityTelaAutenticacaoBinding.inflate(layoutInflater)
        setContentView(menu.root)

        // Botão Criar Conta
        menu.signbuttton.setOnClickListener {
            startActivity(Intent(this, TelaRegistar::class.java))
        }

        // Botão Entrar
        menu.loginbutton.setOnClickListener {
            startActivity(Intent(this, TelaLogin::class.java))
        }

        // para nao voltar ao splash

        onBackPressedDispatcher.addCallback(this) {
            finishAffinity()
        }
    }

    private fun redirecionarUsuarioLogado() {
        val userId = auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        // Buscamos o documento do utilizador no Firestore para saber o "tipo"
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val tipo = document.getString("tipo") // Ex: "Aluno", "Empresa", "Admin"

                when (tipo) {
                    "Aluno" -> {
                        startActivity(Intent(this, MainActivityAluno::class.java))
                        finish()
                    }

                    "Empresa" -> {
                        // Opcional: Verificar se já completou o SetupEmpresa
                        val verificado = document.getBoolean("verificado") ?: false
                        if (verificado) {
                            startActivity(Intent(this, MainActivityEmpresa::class.java))
                        } else {
                            startActivity(Intent(this, SetupEmpresa::class.java))
                        }
                        finish()
                    }

                    "Admin" -> {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }

                    else -> {
                        Toast.makeText(
                            this,
                            "Erro ao identificar tipo de conta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
            .addOnFailureListener {
                Toast.makeText(this, "Erro de conexão ao verificar perfil", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}