class ReservationConflictException(
    val bookId: String,
    message: String = "Indisponível no momento. Outro cliente reservou este item."
) : RuntimeException(message)