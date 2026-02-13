package com.nicolaseduardo.e_commerce_adilson.dto.pix

data class PixCobResponse(
    val loc: Loc?,
    val pixCopiaECola: String?
) { data class Loc(val id: Int?) }

data class PixQrResponse(
    val qrcode: String?,
    val imagemQrcode: String?
)