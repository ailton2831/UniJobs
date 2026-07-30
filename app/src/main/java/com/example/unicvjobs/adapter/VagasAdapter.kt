package com.example.unicvjobs.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.unicvjobs.R
import com.example.unicvjobs.classes.Vaga
import com.example.unicvjobs.vaga.DetalheVaga
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class VagasAdapter(private var listaVagas: List<Vaga>): RecyclerView.Adapter<VagasAdapter.VagasViewHolder>() {

    inner class VagasViewHolder(vagaView: View): RecyclerView.ViewHolder(vagaView) {
        // IDs atualizados para bater exatamente com o seu XML
        val titulo: TextView = vagaView.findViewById(R.id.profissao)
        val empresaNome: TextView = vagaView.findViewById(R.id.nomeEmpresa)
        val tipo: Chip = vagaView.findViewById(R.id.tipoVaga)
        val local: Chip = vagaView.findViewById(R.id.localVaga)
        val status: TextView = vagaView.findViewById(R.id.status)
        val descricao: TextView = vagaView.findViewById(R.id.descricaoVaga)
        val btnDetalhes: MaterialButton = vagaView.findViewById(R.id.buttonDetalhes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VagasViewHolder {
        // Certifique-se que o nome do arquivo XML é activity_item_vaga.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_vaga, parent, false)
        return VagasViewHolder(view)
    }

    override fun onBindViewHolder(holder: VagasViewHolder, position: Int) {
        val vaga = listaVagas[position]

        holder.titulo.text = vaga.titulo
        holder.empresaNome.text = vaga.nomeEmpresa
        holder.tipo.text = vaga.tipo
        holder.local.text = vaga.localizacao
        holder.status.text = vaga.status
        holder.descricao.text = vaga.descricao

        // Estilização dinâmica do Status
        if (vaga.status.lowercase() == "aberta") {
            holder.status.setTextColor(Color.parseColor("#2E7D32")) // Verde
        } else {
            holder.status.setTextColor(Color.parseColor("#C62828")) // Vermelho
        }

        // Clique no botão de detalhes
        holder.btnDetalhes.setOnClickListener {
            val context = holder.itemView.context

            if (vaga.idVaga.isNotEmpty()) {
                val intent = Intent(context, DetalheVaga::class.java).apply {
                    putExtra("ID_VAGA", vaga.idVaga)
                    putExtra("TITULO_VAGA", vaga.titulo)
                    putExtra("ID_EMPRESA", vaga.idEmpresa)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Erro: ID da vaga não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = listaVagas.size

    fun atualizarLista(novaLista: List<Vaga>) {

        listaVagas = novaLista

        notifyDataSetChanged()

    }
}