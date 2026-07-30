package com.example.unicvjobs.empresa

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.empresa.TelaEspera
import com.example.unicvjobs.databinding.ActivitySetupEmpresaBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class SetupEmpresa : AppCompatActivity() {
    private lateinit var menu: ActivitySetupEmpresaBinding
    private lateinit var auth: FirebaseAuth
    private var uriDeclaracao: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivitySetupEmpresaBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore
        val storageRef = Firebase.storage.reference

        // 1. Configuração dos Spinners
        val itenslocal = listOf("Santiago","São Vicente","Sal","Fogo","Brava","Maio","Santo Antão","Boa Vista")
        val adapterlocal = ArrayAdapter(this, R.layout.simple_spinner_item, itenslocal)
        adapterlocal.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerLocal.adapter = adapterlocal

        val itensarea = listOf("Engenharia","Saúde","Educação","Transporte","Finanças","TI","Marketing", "Outro")
        val adapterarea = ArrayAdapter(this, R.layout.simple_spinner_item, itensarea)
        adapterarea.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerArea.adapter = adapterarea

        // 2. Botões de Anexo




        menu.buttonSave.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val nome = menu.inputNome.text.toString().trim()
            val contato = menu.inputContato.text.toString().trim()
            val desc = menu.inputDescricao.text.toString().trim()

            // Validação simples
            if (nome.isEmpty() || contato.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos e anexe o PDF", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Bloqueia o botão para evitar cliques múltiplos
            menu.buttonSave.isEnabled = false
            val dadosCompletos = hashMapOf(
                "nome" to nome,
                "contato" to contato,
                "localizacao" to menu.spinnerLocal.selectedItem.toString(),
                "area" to menu.spinnerArea.selectedItem.toString(),
                "descricao" to desc,
                "verificado" to false,
                "perfilCompleto" to true,
                "status" to false
            )

            db.collection("users").document(uid).update(dadosCompletos as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cadastro enviado para análise!", Toast.LENGTH_LONG).show()


                    val intent = Intent(this, TelaEspera::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    menu.buttonSave.isEnabled = true
                    Toast.makeText(this, "Erro ao salvar no banco", Toast.LENGTH_SHORT).show()
                }
            }


    }


}