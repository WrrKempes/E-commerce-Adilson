package com.nicolaseduardo.e_commerce_adilson.dto.pix

data class PixCobrancaResponse(
    val pixCopiaECola: String,
    val imagemQrcodeBase64: String,
    val txid: String
)