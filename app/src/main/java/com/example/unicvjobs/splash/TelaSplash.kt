package com.example.unicvjobs.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unicvjobs.R
import com.example.unicvjobs.admin.MainActivity
import com.example.unicvjobs.autenticacao.TelaAutenticacao
import com.example.unicvjobs.empresa.MainActivityEmpresa
import com.example.unicvjobs.empresa.SetupEmpresa
import com.example.unicvjobs.empresa.TelaEspera
import com.example.unicvjobs.estudante.MainActivityAluno
import com.example.unicvjobs.estudante.SetupEstudante
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class TelaSplash : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_splash)

        // 1. Inicializar Firebase Auth
        auth = Firebase.auth

        // 2. Referências das Views (Animações)
        val logoCard = findViewById<MaterialCardView>(R.id.logo_card)
        val appName = findViewById<TextView>(R.id.text_app_name)
        val tagline = findViewById<TextView>(R.id.text_tagline)
        val loadingContainer = findViewById<LinearLayout>(R.id.loading_container)
        val versionText = findViewById<TextView>(R.id.text_version)

        // Estados iniciais
        logoCard.apply { scaleX = 0f; scaleY = 0f; alpha = 0f }
        appName.apply { translationY = 40f; alpha = 0f }
        tagline.apply { translationY = 40f; alpha = 0f }
        loadingContainer.alpha = 0f
        versionText.alpha = 0f

        // 3. Iniciar Sequência de Animações
        logoCard.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(800).setInterpolator(
            OvershootInterpolator()
        ).start()

        Handler(Looper.getMainLooper()).postDelayed({
            appName.animate().translationY(0f).alpha(1f).setDuration(800).setInterpolator(
                DecelerateInterpolator()
            ).start()
        }, 200)

        Handler(Looper.getMainLooper()).postDelayed({
            tagline.animate().translationY(0f).alpha(1f).setDuration(800).setInterpolator(
                DecelerateInterpolator()
            ).start()
        }, 400)

        Handler(Looper.getMainLooper()).postDelayed({
            loadingContainer.animate().alpha(1f).setDuration(600).start()
            versionText.animate().alpha(1f).setDuration(600).start()
        }, 800)

        // 4. AGORA A MÁGICA: Após as animações (2.5s), checamos o Login
        Handler(Looper.getMainLooper()).postDelayed({
            val user = auth.currentUser
            if (user != null) {
                // Se está logado, decidimos o destino pelo Firestore
                verificarFluxoEEntrar(user.uid)
            } else {
                // Se não está logado, vai para a autenticação
                startActivity(Intent(this, TelaAutenticacao::class.java))
                finish()
            }
        }, 2500)
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

                    !perfilCompleto -> {
                        if (tipo == "Empresa") Intent(this, SetupEmpresa::class.java)
                        else Intent(this, SetupEstudante::class.java)
                    }
                    tipo == "Empresa" && !status -> Intent(this, TelaEspera::class.java)
                    tipo == "Estudante" -> Intent(this, MainActivityAluno::class.java)
                    tipo == "Empresa"   -> Intent(this, MainActivityEmpresa::class.java)
                    tipo == "Admin"     -> Intent(this, MainActivity::class.java)
                    else -> Intent(this, TelaAutenticacao::class.java)
                }

                startActivity(intent)
                finish()
            } else {
                startActivity(Intent(this, TelaAutenticacao::class.java))
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro de conexão. Tente novamente.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, TelaAutenticacao::class.java))
            finish()
        }
    }
}