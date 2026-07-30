package com.example.unicvjobs.classes

data class Candidatura(
    var idCandidatura: String = "",
    val idVaga: String = "",
    val nome: String = "",
    val universidade: String = "",
    val curso: String = "",
    val cvUrl: String = "",
    var status: String = "Pendente"
)