package com.nicolaseduardo.e_commerce_adilson.exceptions

class ReservationConflictException(
    val bookId: String,
    message: String = "Indisponível no momento. Outro cliente reservou este item."
) : RuntimeException(message)