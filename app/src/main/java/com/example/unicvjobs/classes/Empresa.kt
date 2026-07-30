package com.example.unicvjobs.classes

data class Empresa (
    val nome: String = "",
    val contato: String = "",
    val status: Boolean = false,
    val localizacao: String = "",
    val area: String = "",
    val descriçao: String ="",
    val urlDocumento: String = "",
    var idDocumento: String = ""
)