package com.example.unicvjobs.adapter

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.unicvjobs.R
import com.example.unicvjobs.classes.Candidatura
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class CandidaturaAdapter(var listaCandidatura: List<Candidatura>): RecyclerView.Adapter<CandidaturaAdapter.CandidaturaViewHolder>() {

    inner class CandidaturaViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val nome: TextView = itemView.findViewById(R.id.RV2nome)
        val curso: TextView = itemView.findViewById(R.id.RV2curso)
        val uni: TextView = itemView.findViewById(R.id.RV2uni)
        val status: TextView = itemView.findViewById(R.id.RV2status)
        val aprovar: View = itemView.findViewById(R.id.aprovar)
        val reprovar: View = itemView.findViewById(R.id.reprovar)
        val verCv: View = itemView.findViewById(R.id.verCv) // novo botão/TextView no layout candidatura.xml
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidaturaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.candidatura, parent, false)
        return CandidaturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidaturaViewHolder, position: Int) {
        val candidatura = listaCandidatura[position]
        val db = Firebase.firestore
        val context = holder.itemView.context

        holder.nome.text = candidatura.nome
        holder.uni.text = candidatura.universidade
        holder.curso.text = candidatura.curso
        holder.status.text = candidatura.status

        val statusCard = holder.status.parent as? MaterialCardView
        val jaProcessado = candidatura.status != "Pendente"

        when (candidatura.status.uppercase()) {
            "APROVADO" -> statusCard?.setCardBackgroundColor(Color.parseColor("#2E7D32")) // Verde
            "REPROVADO" -> statusCard?.setCardBackgroundColor(Color.parseColor("#C62828")) // Vermelho
            else -> statusCard?.setCardBackgroundColor(Color.parseColor("#FF9800")) // Laranja
        }

        holder.aprovar.visibility = if (jaProcessado) View.GONE else View.VISIBLE
        holder.reprovar.visibility = if (jaProcessado) View.GONE else View.VISIBLE

        // --- VER Curriculum (abre o PDF no navegador/visualizador padrão do Android)
        holder.verCv.visibility = if (candidatura.cvUrl.isNotEmpty()) View.VISIBLE else View.GONE
        holder.verCv.setOnClickListener {
            if (candidatura.cvUrl.isNotEmpty()) {
                val uri = Uri.parse(candidatura.cvUrl)

                // Configura o Intent especificando que é um PDF
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/pdf")
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION

                try {
                    // Tenta abrir com um leitor de PDF nativo do celular
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Se o usuário não tiver leitor de PDF instalado, faz o fallback para o navegador
                    val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(fallbackIntent)
                }
            } else {
                Toast.makeText(context, "Este candidato não anexou CV", Toast.LENGTH_SHORT).show()
            }
        }

        // --- AÇÃO: APROVAR ---
        holder.aprovar.setOnClickListener {
            val docId = candidatura.idCandidatura.ifEmpty { null } ?: return@setOnClickListener

            holder.aprovar.isEnabled = false
            db.collection("candidaturas").document(docId)
                .update("status", "Aprovado")
                .addOnSuccessListener {
                    candidatura.status = "Aprovado"
                    notifyItemChanged(position)
                    Toast.makeText(context, "${candidatura.nome} Aprovado!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    holder.aprovar.isEnabled = true
                    Toast.makeText(context, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
                }
        }

        // --- REPROVAR ---
        holder.reprovar.setOnClickListener {
            val docId = candidatura.idCandidatura.ifEmpty { null } ?: return@setOnClickListener

            holder.reprovar.isEnabled = false
            db.collection("candidaturas").document(docId)
                .update("status", "Reprovado")
                .addOnSuccessListener {
                    candidatura.status = "Reprovado"
                    notifyItemChanged(position)
                    Toast.makeText(context, "Candidatura Rejeitada", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int {
        return listaCandidatura.size
    }
}
