import org.springframework.stereotype.Service

/**
 * Facade para envio de emails de PIX.
 * Delega para classes específicas: PixClientEmailSender e PixAuthorEmailSender
 */
@Service
class PixEmailService(
    private val clientSender: PixClientEmailSender,
    private val authorSender: PixAuthorEmailSender
) {

    /**
     * Envia email para cliente quando Pix é confirmado
     */
    fun sendPixClientEmail(order: Order) {
        clientSender.send(order)
    }

    /**
     * Envia email para autor quando Pix é confirmado
     */
    fun sendPixAuthorEmail(order: Order) {
        authorSender.send(order)
    }

}