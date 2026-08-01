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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Candidato : AppCompatActivity() {

    private lateinit var menu: ActivityCandidatoBinding
    private lateinit var auth: FirebaseAuth

    // Dados do seu Cloudinary
    private val CLOUD_NAME = "tgkwiqux"
    private val UPLOAD_PRESET = "Unijobs"

    private val okHttpClient = OkHttpClient()

    // URI do PDF escolhido pelo aluno (fica só em memória até enviar)
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

        val idVaga = intent.getStringExtra("ID_VAGA") ?: ""
        val tituloVaga = intent.getStringExtra("TITULO_VAGA") ?: "Vaga"

        // Configurar Spinner
        val itensuni = listOf("Universidade de Cabo Verde","Universidade Jean Piaget de Cabo Verde","ISCEE","ISCJJS","Universidade do Mindelo","Universidade de Santiago","IDCS")
        val adapteruni = ArrayAdapter(this, R.layout.simple_spinner_item, itensuni)
        adapteruni.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        menu.spinnerUni.adapter = adapteruni

        // botão "Anexar CV (PDF)"
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

            val uriEscolhido = cvUri
            if (uriEscolhido == null) {
                Toast.makeText(this, "Anexe o seu CV em PDF antes de enviar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            menu.buttonCandidatar.isEnabled = false
            Toast.makeText(this, "Enviando currículo...", Toast.LENGTH_SHORT).show()

            uploadPdfParaCloudinary(
                uriEscolhido,
                onSucesso = { cvUrl ->
                    val candidatura = hashMapOf(
                        "nome" to nomeFinal,
                        "curso" to cursoFinal,
                        "universidade" to uniFinal,
                        "idVaga" to idVaga,
                        "idAluno" to uid,
                        "status" to "Pendente",
                        "tituloVaga" to tituloVaga,
                        "cvUrl" to cvUrl
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
                },
                onErro = { mensagem ->
                    runOnUiThread {
                        menu.buttonCandidatar.isEnabled = true
                        Toast.makeText(this, "Erro ao enviar CV: $mensagem", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }

    /**
     * Copia o PDF escolhido (Uri do content resolver) para um arquivo temporário
     * e sobe pro Cloudinary via upload "unsigned". Roda em thread separada.
     */
    private fun uploadPdfParaCloudinary(
        uri: Uri,
        onSucesso: (String) -> Unit,
        onErro: (String) -> Unit
    ) {
        Thread {
            try {
                // 1. Copia o conteúdo do Uri para um arquivo temporário local
                val tempFile = File(cacheDir, "cv_temp_${System.currentTimeMillis()}.pdf")
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IOException("Não foi possível ler o arquivo selecionado")

                // Limite simples de 5MB (mesmo aviso que já está na tela)
                if (tempFile.length() > 5 * 1024 * 1024) {
                    onErro("O arquivo deve ter no máximo 5MB")
                    tempFile.delete()
                    return@Thread
                }

                // 2. Monta a requisição multipart pro endpoint "raw" do Cloudinary (PDFs = resource_type raw)
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart(
                        "file",
                        tempFile.name,
                        tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/auto/upload")
                    .post(requestBody)
                    .build()

                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        tempFile.delete()
                        onErro(e.message ?: "Falha de rede")
                    }

                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        tempFile.delete()
                        response.use {
                            if (!it.isSuccessful) {
                                onErro("Cloudinary respondeu ${it.code}")
                                return
                            }
                            val body = it.body?.string()
                            val json = JSONObject(body ?: "{}")
                            val secureUrl = json.optString("secure_url")
                            if (secureUrl.isNotEmpty()) {
                                onSucesso(secureUrl)
                            } else {
                                onErro("Resposta do Cloudinary sem URL")
                            }
                        }
                    }
                })

            } catch (e: Exception) {
                onErro(e.message ?: "Erro desconhecido ao preparar upload")
            }
        }.start()
    }
}
