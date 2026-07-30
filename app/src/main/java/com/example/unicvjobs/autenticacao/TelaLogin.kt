package com.example.unicvjobs.autenticacao

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.R
import com.example.unicvjobs.admin.MainActivity
import com.example.unicvjobs.databinding.ActivityTelaLoginBinding
import com.example.unicvjobs.empresa.MainActivityEmpresa
import com.example.unicvjobs.empresa.SetupEmpresa
import com.example.unicvjobs.empresa.TelaEspera
import com.example.unicvjobs.estudante.MainActivityAluno
import com.example.unicvjobs.estudante.SetupEstudante
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class TelaLogin : AppCompatActivity() {
    private lateinit var menu: ActivityTelaLoginBinding
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityTelaLoginBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth

        // 1. Botão Voltar
        menu.buttonBack.setOnClickListener { finish() }

        // 2. Alternar Visibilidade da Senha
        menu.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            menu.inputsenha.inputType = if (isPasswordVisible) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            val icon = if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            menu.btnTogglePassword.setImageResource(icon)
            menu.inputsenha.setSelection(menu.inputsenha.text.length)
        }

        // 3. Lógica de Login
        menu.buttonlogin.setOnClickListener {
            val email = menu.inputusername.text.toString().trim()
            val senha = menu.inputsenha.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            menu.buttonlogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        userId?.let { verificarFluxoEEntrar(it) }
                    } else {
                        menu.buttonlogin.isEnabled = true
                        Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // 4. Ir para Tela de Registro
        menu.txtCriarConta.setOnClickListener {
            startActivity(Intent(this, TelaRegistar::class.java))
        }
    }

    private fun verificarFluxoEEntrar(userId: String) {
        val db = Firebase.firestore

        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val tipo = document.getString("tipo") ?: ""
                val perfilCompleto = document.getBoolean("perfilCompleto") ?: false
                val status = document.getBoolean("status") ?: false

                val intent = when {

                    tipo == "Admin" -> Intent(this, MainActivity::class.java)
                    // PASSO 1: Se o perfil está incompleto (independente de ser aluno ou empresa)
                    !perfilCompleto -> {
                        if (tipo == "Empresa") Intent(this, SetupEmpresa::class.java)
                        else Intent(this, SetupEstudante::class.java)
                    }

                    // PASSO 2: Perfil completo, mas se for Empresa, checa aprovação do Admin
                    tipo == "Empresa" && !status -> {
                        Intent(this, TelaEspera::class.java)
                    }

                    // PASSO 3: Se chegou aqui, está tudo OK. Vai para a Home correta
                    tipo == "Estudante" -> Intent(this, MainActivityAluno::class.java)
                    tipo == "Empresa"   -> Intent(this, MainActivityEmpresa::class.java)
                    tipo == "Admin"     -> Intent(this, MainActivity::class.java)
                    else -> null
                }

                if (intent != null) {
                    startActivity(intent)
                    finish()
                } else {
                    menu.buttonlogin.isEnabled = true
                    Toast.makeText(this, "Erro ao identificar fluxo do usuário.", Toast.LENGTH_SHORT).show()
                }
            } else {
                menu.buttonlogin.isEnabled = true
                Toast.makeText(this, "Dados do usuário não encontrados.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            menu.buttonlogin.isEnabled = true
            Toast.makeText(this, "Erro ao conectar ao banco de dados.", Toast.LENGTH_SHORT).show()
        }
    }
}