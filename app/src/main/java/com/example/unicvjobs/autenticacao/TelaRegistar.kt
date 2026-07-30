package com.example.unicvjobs.autenticacao

import android.R
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.databinding.ActivityTelaRegistarBinding
import com.example.unicvjobs.empresa.SetupEmpresa
import com.example.unicvjobs.estudante.SetupEstudante
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class TelaRegistar : AppCompatActivity() {
    private lateinit var menu: ActivityTelaRegistarBinding
    private lateinit var auth: FirebaseAuth
    private var senha1Visivel = false
    private var senha2Visivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityTelaRegistarBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore

        // 1. Configurar Spinner
        val itenstipo = listOf("Estudante", "Empresa")
        val adaptertipo = ArrayAdapter(this, R.layout.simple_spinner_item, itenstipo)
        adaptertipo.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerTipo.adapter = adaptertipo

        // 2. Botão Voltar
        menu.btnBack.setOnClickListener { finish() }
        menu.textLogin.setOnClickListener { finish() }

        // 3. Toggles de Visibilidade de Senha
        menu.btnTogglePassword1.setOnClickListener {
            senha1Visivel = !senha1Visivel
            menu.inputsenha.inputType = if (senha1Visivel) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            menu.inputsenha.setSelection(menu.inputsenha.text.length)
        }

        menu.btnTogglePassword2.setOnClickListener {
            senha2Visivel = !senha2Visivel
            menu.inputsenha2.inputType = if (senha2Visivel) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            menu.inputsenha2.setSelection(menu.inputsenha2.text.length)
        }

        menu.textLogin.setOnClickListener {
            startActivity(Intent(this, TelaLogin::class.java))
        }

        // 4. Lógica de Registo
        menu.buttonsign.setOnClickListener {
            val email = menu.inputemail.text.toString().trim()
            val senha = menu.inputsenha.text.toString()
            val senha2 = menu.inputsenha2.text.toString()
            val tipo = menu.spinnerTipo.selectedItem.toString()

            if (email.isEmpty() || senha.isEmpty() || senha2.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha != senha2) {
                menu.inputsenha2.error = "As senhas não coincidem"
                return@setOnClickListener
            }

            if (senha.length < 8) {
                menu.inputsenha.error = "Mínimo 8 caracteres"
                return@setOnClickListener
            }


            menu.buttonsign.isEnabled = false

            auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val userMap = hashMapOf(
                            "uid" to userId,
                            "email" to email,
                            "tipo" to tipo,
                            "status" to (tipo != "Empresa"), // Empresa começa como false (pendente)
                            "perfilCompleto" to false
                        )


                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                redirecionarParaSetup(tipo)
                            }
                            .addOnFailureListener {
                                menu.buttonsign.isEnabled = true
                                Toast.makeText(this, "Erro ao guardar dados", Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        menu.buttonsign.isEnabled = true
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Este e-mail já está registado.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun redirecionarParaSetup(tipo: String) {
        val intent = if (tipo == "Estudante") {
            Intent(this, SetupEstudante::class.java)
        } else {
            Intent(this, SetupEmpresa::class.java)
        }
        startActivity(intent)
        finish()
    }
}