import com.nicolaseduardo.e_commerce_adilson.repositories.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CardEfiWebhookService(
    private val orderRepository: OrderRepository,
    private val processor: CardPaymentProcessor
) {
    /** Payload mínimo esperado do webhook de CARTÃO da Efí. */
    data class EfiCardWebhookPayload(val data: CardData?) {
        data class CardData(
            val charge_id: Any?, // pode vir número ou string
            val status: String?
        )
    }

    @Transactional
    fun handleWebhook(payload: EfiCardWebhookPayload) {
        val chargeId = payload.data?.charge_id?.toString()?.trim().orEmpty()
        if (chargeId.isEmpty()) return

        val newStatus = OrderStatus.fromEfi(payload.data?.status)
        val order = orderRepository.findByChargeId(chargeId) ?: return

        // 1) Se for status de PAGAMENTO (Paid/Approved), delegamos para o Processor Central
        // Ele já faz: validação status/TTL, marca pago, emails, e agendamento de repasse.
        if (processor.isCardPaidStatus(newStatus.toEfiStatus())) {
            processor.markPaidIfNeededByChargeId(chargeId)
            return
        }

        // 2) Outros status (ex: recusado, estornado) tratamos apenas atualizando o banco
        // Não regredir estados finais a menos que seja melhoria (pago-like) - mas aqui já tratamos
        // pago acima
        if (order.status.isFinal()) return

        order.status = newStatus

        if (newStatus in arrayOf(_root_ide_package_.com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus.REFUNDED, _root_ide_package_.com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus.PARTIALLY_REFUNDED)) {
            // já houve pagamento; mantemos paid=true indicando que o fluxo de pagamento ocorreu
            order.paid = true
        } else if (newStatus in
            arrayOf(
                _root_ide_package_.com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus.CANCELED,
                _root_ide_package_.com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus.DECLINED,
                _root_ide_package_.com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus.UNPAID,
                _root_ide_package_.com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus.EXPIRED
            )
        ) {
            order.paid = false
        }

        orderRepository.save(order)
    }

    // Helper simples para reverter o enum para string se necessário, ou usar direto strings da Efi
    // se disponível
    private fun com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus.toEfiStatus(): String =
        when (this) {
            _root_ide_package_.com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus.PAID -> "paid"
            _root_ide_package_.com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus.CONFIRMED -> "approved" // Aproximação
            else -> this.name
        }
}