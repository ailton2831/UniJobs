package com.example.unicvjobs.estudante

import android.R
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.databinding.ActivityCandidatoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.util.UUID

class Candidato : AppCompatActivity() {

    private lateinit var menu: ActivityCandidatoBinding
    private lateinit var auth: FirebaseAuth

    private var cvUri: Uri? = null


    private val selecionarPdfLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                cvUri = uri
                menu.textCvSelecionado.text = "CV selecionado: ${uri.lastPathSegment}"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        menu = ActivityCandidatoBinding.inflate(layoutInflater)
        setContentView(menu.root)

        auth = Firebase.auth
        val db = Firebase.firestore
        val storageRef = Firebase.storage.reference

        val idVaga = intent.getStringExtra("ID_VAGA") ?: ""
        val tituloVaga = intent.getStringExtra("TITULO_VAGA") ?: "Vaga"

        // Configurar Spinner
        val itensuni = listOf("Universidade de Cabo Verde","Universidade Jean Piaget de Cabo Verde","ISCEE","ISCJJS","Universidade do Mindelo","M_EIA","Universidade de Santiago","IDCS")
        val adapteruni = ArrayAdapter(this, R.layout.simple_spinner_item, itensuni)
        adapteruni.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerUni.adapter = adapteruni

        // botão "Anexar CV (PDF)"  abre o seletor de arquivos só para PDF
        menu.buttonAnexarCv.setOnClickListener {
            selecionarPdfLauncher.launch("application/pdf")
        }

        // candidatar
        menu.buttonCandidatar.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val nomeFinal = menu.inputNome.text.toString().trim()
            val cursoFinal = menu.inputCurso.text.toString().trim()
            val uniFinal = menu.spinnerUni.selectedItem?.toString() ?: ""

            if (nomeFinal.isEmpty() || cursoFinal.isEmpty()) {
                Toast.makeText(this, "Preencha tudo e anexe o currículo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cvUri == null) {
                Toast.makeText(this, "Anexe o seu CV em PDF antes de enviar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            menu.buttonCandidatar.isEnabled = false
            Toast.makeText(this, "Enviando currículo...", Toast.LENGTH_SHORT).show()

            // 1) Sobe o PDF para o Storage, em cvs/{uid}/{idVaga}_{uuid}.pdf
            val nomeArquivo = "${idVaga}_${UUID.randomUUID()}.pdf"
            val cvRef = storageRef.child("cvs/$uid/$nomeArquivo")

            cvRef.putFile(cvUri!!)
                .addOnSuccessListener {
                    // 2) Pega a URL pública de download
                    cvRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // 3) Só agora grava a candidatura no Firestore, já com o link do CV
                        val candidatura = hashMapOf(
                            "nome" to nomeFinal,
                            "curso" to cursoFinal,
                            "universidade" to uniFinal,
                            "idVaga" to idVaga,
                            "idAluno" to uid,
                            "status" to "Pendente",
                            "tituloVaga" to tituloVaga,
                            "cvUrl" to downloadUri.toString()
                        )
                        db.collection("candidaturas").add(candidatura)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Candidatura enviada!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                menu.buttonCandidatar.isEnabled = true
                                Toast.makeText(this, "Erro ao salvar no banco", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    menu.buttonCandidatar.isEnabled = true
                    Toast.makeText(this, "Erro ao enviar o CV: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
