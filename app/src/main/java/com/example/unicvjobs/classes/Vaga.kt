package com.example.unicvjobs.classes

data class Vaga (
    val titulo: String = "",
    val nomeEmpresa: String = "",
    val tipo: String = "",
    val localizacao: String = "",
    val status: String = "Aberta",
    val descricao: String = "",
    val idEmpresa: String = "",
    var idVaga: String = ""
)