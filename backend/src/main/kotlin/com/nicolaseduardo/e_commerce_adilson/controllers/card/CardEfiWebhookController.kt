package com.nicolaseduardo.e_commerce_adilson.controllers.card

import CardPaymentProcessor
import OrderStatusEmailService
import PaymentTriggerService
import PayoutEmailRepository
import WebhookEventRepository
import com.fasterxml.jackson.databind.ObjectMapper

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import com.nicolaseduardo.e_commerce_adilson.models.order.OrderStatus
import com.nicolaseduardo.e_commerce_adilson.models.payout.PayoutEmailType
import com.nicolaseduardo.e_commerce_adilson.models.webhook.WebhookEvent
import com.nicolaseduardo.e_commerce_adilson.repositories.OrderRepository
import com.nicolaseduardo.e_commerce_adilson.web.ApiRoutes
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
@RequestMapping("${ApiRoutes.API_V1}/webhooks/payment")
class CardEfiWebhookController(
    private val mapper: ObjectMapper,
    private val orders: OrderRepository,
    private val processor: CardPaymentProcessor,
    private val webhookRepo: WebhookEventRepository,
    private val payoutEmailRepo: PayoutEmailRepository,
    private val orderStatusEmailService: OrderStatusEmailService,
    // 🔌 Injeção do orquestrador (mantém OCP; decisão de "quando" chamar fica fora do controller)
    private val payoutTrigger: PaymentTriggerService,
    private val payoutCardEmailService: PayoutCardEmailService
) {
    private val log = LoggerFactory.getLogger(CardEfiWebhookController::class.java)

    @PostMapping("/card", consumes = ["application/json"])
    @Transactional
    fun handle(@RequestBody rawBody: String): ResponseEntity<String> {
        log.info("EFI CARD WEBHOOK RAW={}", rawBody.take(4000))

        val root = runCatching { mapper.readTree(rawBody) }.getOrElse {
            log.warn("CARD WEBHOOK: JSON inválido: {}", it.message)
            webhookRepo.save(
                WebhookEvent(
                    txid = null,
                    status = "INVALID_JSON",
                    chargeId = null,
                    provider = "CARD",
                    rawBody = rawBody,
                    receivedAt = OffsetDateTime.now()
                )
            )
            return ResponseEntity.ok("ignored: invalid json")
        }

        val chargeId = listOf(
            root.path("charge_id"),
            root.path("data").path("charge_id"),
            root.path("identifiers").path("charge_id"),
            root.path("charge").path("id"),
            root.path("data").path("charge").path("id"),
            root.path("payment").path("charge_id")
        ).firstOrNull { !it.isMissingNode && !it.isNull && it.asText().isNotBlank() }?.asText()

        val status = listOf(
            root.path("status"),
            root.path("data").path("status"),
            root.path("payment").path("status"),
            root.path("charge").path("status"),
            root.path("data").path("charge").path("status"),
            root.path("transaction").path("status")
        ).firstOrNull { !it.isMissingNode && !it.isNull && it.asText().isNotBlank() }?.asText()

        // Resolve o pedido (se possível) para também armazenar o txid no histórico
        val order = chargeId?.let { orders.findWithItemsByChargeId(it) }

        // Persistimos SEMPRE para auditoria
        webhookRepo.save(
            WebhookEvent(
                txid = order?.txid,           // se achou o pedido, guarda o txid dele
                status = status,
                rawBody = rawBody,
                chargeId = chargeId,
                provider = "CARD",
                receivedAt = OffsetDateTime.now()
            )
        )

        if (chargeId == null) {
            log.info("CARD WEBHOOK: ignorado, sem charge_id")
            return ResponseEntity.ok("ignored: no charge_id")
        }
        if (status == null) {
            log.info("CARD WEBHOOK: ignorado, sem status (chargeId={})", chargeId)
            return ResponseEntity.ok("ignored: no status")
        }
        if (order == null) {
            log.info("CARD WEBHOOK: order not found for chargeId={}, status={}", chargeId, status)
            return ResponseEntity.ok("ignored: order not found")
        }

        val paid = processor.isCardPaidStatus(status)
        val declined = processor.isCardDeclinedStatus(status)

        // Atualiza status do pedido baseado no status do webhook
        val orderStatus = OrderStatus.fromEfi(status)
        val previousStatus = order.status
        val wasPaid = order.paid

        // Atualiza status do pedido se necessário (mesmo que não seja pago ainda)
        if (order.status != orderStatus && !orderStatus.isFinal() || orderStatus.isPaidLike()) {
            order.status = orderStatus
            orders.save(order)
            log.info("CARD WEBHOOK: status atualizado orderId={}, previousStatus={}, newStatus={}", order.id, previousStatus, orderStatus)
        }

        // Processa pagamento aprovado
        val applied = if (paid) {
            processor.markPaidIfNeededByChargeId(chargeId)
        } else {
            false
        }

        // Processa pagamento rejeitado
        if (declined && !wasPaid && order.id != null) {
            // Atualiza status para DECLINED se ainda não estava pago
            if (order.status != OrderStatus.DECLINED) {
                order.status = OrderStatus.DECLINED
                orders.save(order)
            }

            // Envia email de pagamento rejeitado
            runCatching {
                orderStatusEmailService.sendFailedEmail(
                    order = order,
                    reason = "Pagamento recusado pela operadora. Status: $status"
                )
                log.info("CARD WEBHOOK: email de pagamento rejeitado enviado para order #{}", order.id)
            }.onFailure { e ->
                log.error("CARD WEBHOOK: falha ao enviar email de pagamento rejeitado (orderId={}, chargeId={}): {}", order.id, chargeId, e.message)
            }
        }

        // 🔔 EMAIL IMEDIATO (CARTÃO): informa sobre repasse D+32
        // Nota: O email já é enviado no CardPaymentProcessor quando o pagamento é confirmado.
        // Aqui só enviamos se ainda não foi enviado (caso o webhook chegue antes do one-step confirmar).
        if (paid && applied && order.id != null) {
            // Verifica se o email de repasse agendado já foi enviado
            val alreadySent = payoutEmailRepo.findByOrderIdAndEmailType(
                order.id!!,
                PayoutEmailType.REPASSE_CARD.name
            ).isNotEmpty()

            if (!alreadySent) {
                runCatching {
                    // Envia email informando que o repasse será processado em 32 dias
                    payoutCardEmailService.sendPayoutScheduledEmail(
                        orderId = order.id!!,
                        amount = order.total,
                        payeePixKey = null, // Será resolvido pelo PaymentTriggerService
                        idEnvio = "C${order.id}",
                        extraNote = "Repasse programado para 32 dias (política Efí Bank)"
                    )

                    log.info("CARD PAYOUT EMAIL [WEBHOOK]: Enviado para order #{} (D+32)", order.id)
                }.onFailure { e ->
                    log.error("CARD WEBHOOK: falha ao enviar email de repasse (orderId={}, chargeId={}): {}", order.id, chargeId, e.message)
                }
            } else {
                log.debug("CARD WEBHOOK: email de repasse agendado já foi enviado para order #{} (ignorando duplicação)", order.id)
            }
        }

        log.info(
            "CARD WEBHOOK: chargeId={}, status={}, paidLike={}, declined={}, applied={}, orderId={}, orderStatus={}",
            chargeId, status, paid, declined, applied, order.id, order.status
        )
        return ResponseEntity.ok("status=$status; applied=$applied; declined=$declined")
    }

    private fun mask(pixKey: String): String {
        return if (pixKey.length <= 6) {
            "***"
        } else {
            pixKey.take(3) + "***" + pixKey.takeLast(3)
        }
    }
}