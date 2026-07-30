package com.example.unicvjobs.estudante

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.databinding.ActivitySetupEstudanteBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class SetupEstudante : AppCompatActivity() {

    private lateinit var menu: ActivitySetupEstudanteBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivitySetupEstudanteBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore

        // 1. Configurar Spinners
        val itensuni = listOf("Universidade de Cabo Verde","Universidade Jean Piaget","ISCEE","ISCJJS","Universidade do Mindelo","M_EIA","Universidade de Santiago","IDCS")
        val adapteruni = ArrayAdapter(this, R.layout.simple_spinner_item, itensuni)
        adapteruni.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerUni.adapter = adapteruni

        val itensgrau = listOf("Licenciatura", "Mestrado", "Doutoramento")
        val adaptergrau = ArrayAdapter(this, R.layout.simple_spinner_item, itensgrau)
        adaptergrau.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerGrau.adapter = adaptergrau

        // 2. Botão Guardar
        menu.buttonSave.setOnClickListener {
            // CAPTURA OS DADOS AQUI (quando o usuário clica)
            val nome = menu.inputNome.text.toString()
            val idade = menu.inputIdade.text.toString()
            val curso = menu.inputCurso.text.toString()
            val uni = menu.spinnerUni.selectedItem.toString()
            val grau = menu.spinnerGrau.selectedItem.toString()
            val userId = auth.currentUser?.uid

            if (nome.isEmpty() || idade.isEmpty() || curso.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else if (userId != null) {

                val setup = hashMapOf(
                    "nome" to nome,
                    "curso" to curso,
                    "idade" to idade,
                    "universidade" to uni,
                    "grau" to grau,
                    "perfilCompleto" to true
                )

                // Atualiza diretamente pelo UID do usuário
                db.collection("users").document(userId).update(setup as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivityAluno::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao salvar dados", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // 3. Botão Pular
        menu.buttonSkip.setOnClickListener {
            startActivity(Intent(this, MainActivityAluno::class.java))
            finish()
        }
    }
}