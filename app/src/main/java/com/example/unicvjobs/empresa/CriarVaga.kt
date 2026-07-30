package com.example.unicvjobs.empresa

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.databinding.ActivityCriarVagaBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class CriarVaga : AppCompatActivity() {

    private lateinit var menu: ActivityCriarVagaBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        menu = ActivityCriarVagaBinding.inflate(layoutInflater)

        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore

        val itenslocal = mutableListOf("Santiago","São Vicente","Sal","Fogo","Brava","Maio","Santo Antão","Boa Vista")
        val adapterlocal = ArrayAdapter(this, R.layout.simple_spinner_item, itenslocal)
        adapterlocal.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerLocalVaga.adapter = adapterlocal

        val itensarea = mutableListOf("Engenharia","Saúde","Educação","Transporte","Finanças","Recursos Humanos","TI","Marketing","Construção Civil","Outro")
        val adapterarea = ArrayAdapter(this, R.layout.simple_spinner_item, itensarea)
        adapterarea.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerAreaVaga.adapter = adapterarea

        val itenstipo = mutableListOf("Emprego","Estágio","Part-time","Full-time")
        val adaptertipo = ArrayAdapter(this, R.layout.simple_spinner_item, itenstipo)
        adaptertipo.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerTipoVaga.adapter = adaptertipo




        fun salvarVaga(nome: String) {
            val titulo = menu.inputTituloVaga.text.toString()
            val tipo = menu.spinnerTipoVaga.selectedItem.toString()
            val local = menu.spinnerLocalVaga.selectedItem.toString()
            val area = menu.spinnerAreaVaga.selectedItem.toString()
            val descricao = menu.inputDescricao.text.toString()
            val status = "Aberta"
            val user = auth.currentUser?.uid ?: return

            if (titulo.isEmpty() || descricao.isEmpty()) {
                Toast.makeText(this, "Preencha título e descrição", Toast.LENGTH_SHORT).show()
                // Reativa o botão se houver erro
                menu.buttonAddVaga.isEnabled = true
                menu.buttonAddVaga.text = "Publicar Vaga"
                return
            }

            val vaga = hashMapOf(
                "titulo" to titulo,
                "tipo" to tipo,
                "localizacao" to local,
                "area" to area,
                "descricao" to descricao,
                "status" to status,
                "idEmpresa" to user,
                "nomeEmpresa" to nome
            )

            db.collection("vagas").add(vaga)
                .addOnSuccessListener {
                    Toast.makeText(this, "Vaga publicada com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    menu.buttonAddVaga.isEnabled = true
                    menu.buttonAddVaga.text = "Publicar Vaga"
                    Toast.makeText(this, "Erro ao publicar", Toast.LENGTH_SHORT).show()
                }
        }

        fun publicarVaga() {
            // Bloquear o botão para evitar cliques duplos
            menu.buttonAddVaga.isEnabled = false
            menu.buttonAddVaga.text = "A publicar..."

            val user = auth.currentUser?.uid ?: return

            db.collection("users").document(user).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nome = document.getString("nome") ?: "Empresa Confidencial"
                        salvarVaga(nome)
                    } else {
                        // Se não achar a empresa, reativa o botão
                        menu.buttonAddVaga.isEnabled = true
                        Toast.makeText(this, "Erro: Perfil da empresa não encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    menu.buttonAddVaga.isEnabled = true
                    Toast.makeText(this, "Erro de conexão", Toast.LENGTH_SHORT).show()
                }
        }

        menu.buttonAddVaga.setOnClickListener {
            publicarVaga()
        }


        val nav = menu.navBarEmpresa

        nav.selectedItemId = com.example.unicvjobs.R.id.criar_vagas

        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.unicvjobs.R.id.vagas -> {
                    finish()
                    true
                }
                com.example.unicvjobs.R.id.perfil -> {
                    startActivity(Intent(this, PerfilEmpresa::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

    }
}