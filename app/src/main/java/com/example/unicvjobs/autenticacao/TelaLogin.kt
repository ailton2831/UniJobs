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

        menu.buttonBack.setOnClickListener { finish() }

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

                val isAluno = tipo.equals("Estudante", ignoreCase = true) || tipo.equals("Aluno", ignoreCase = true)
                val isEmpresa = tipo.equals("Empresa", ignoreCase = true)
                val isAdmin = tipo.equals("Admin", ignoreCase = true)

                val intent = when {
                    isAdmin -> Intent(this, MainActivity::class.java)

                    !perfilCompleto -> {
                        if (isEmpresa) Intent(this, SetupEmpresa::class.java)
                        else Intent(this, SetupEstudante::class.java)
                    }

                    isEmpresa && !status -> Intent(this, TelaEspera::class.java)

                    isAluno -> Intent(this, MainActivityAluno::class.java)
                    isEmpresa -> Intent(this, MainActivityEmpresa::class.java)
                    else -> null
                }

                if (intent != null) {
                    startActivity(intent)
                    finish()
                } else {
                    menu.buttonlogin.isEnabled = true
                    Toast.makeText(this, "Tipo de usuário não reconhecido: $tipo", Toast.LENGTH_SHORT).show()
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