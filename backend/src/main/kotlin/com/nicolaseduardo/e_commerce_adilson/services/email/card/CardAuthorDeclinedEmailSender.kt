package com.nicolaseduardo.e_commerce_adilson.services.email.card

import CardEmailBase
import com.nicolaseduardo.e_commerce_adilson.models.order.Order
import com.nicolaseduardo.e_commerce_adilson.services.book.BookService
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

/**
 * Responsável por enviar email para autor quando cartão é recusado
 */
@Component
class CardAuthorDeclinedEmailSender(
    mailSender: JavaMailSender,
    bookService: BookService,
    @Value("\${email.author}") authorEmail: String,
    @Value("\${application.brand.name:Adilson Machado - E-Commerce}") brandName: String,
    @Value("\${mail.from:}") configuredFrom: String,
    @Value("\${mail.logo.url:https://www.andescoresoftware.com.br/AndesCore.jpg}") logoUrl: String
) : CardEmailBase(mailSender, bookService, authorEmail, brandName, configuredFrom, logoUrl) {

    fun send(order: Order) {
        val subject = "⚠️ Pedido recusado no cartão (#${order.id}) — $brandName"
        val html = buildHtml(order)
        CardEmailBase.sendEmail(to = authorEmail, subject = subject, html = html)
    }

    private fun buildHtml(order: Order): String {
        val phoneDigits = CardEmailBase.onlyDigits(order.phone)
        val nationalPhone = CardEmailBase.normalizeBrPhone(phoneDigits)
        val maskedPhone = CardEmailBase.maskCelularBr(nationalPhone.ifEmpty { order.phone })
        val waHref = if (nationalPhone.length == 11) "https://wa.me/55$nationalPhone" else "https://wa.me/55$phoneDigits"

        val addressLine = CardEmailBase.buildAddressLine(order)
        val noteBlock = order.note?.takeIf { it.isNotBlank() }?.let {
            """<p style="margin:10px 0 0"><strong>📝 Observação do cliente:</strong><br>${CardEmailBase.escapeHtml(it)}</p>"""
        } ?: ""

        val header = """
            <p style="margin:0 0 10px"><strong>⚠️ Pedido recusado no cartão</strong>.</p>
            <p style="margin:0 0 4px">👤 Cliente: ${CardEmailBase.escapeHtml(order.firstName)} ${
            CardEmailBase.escapeHtml(
                order.lastName
            )
        }</p>
            <p style="margin:0 0 4px">✉️ Email: ${CardEmailBase.escapeHtml(order.email)}</p>
            <p style="margin:0 0 4px">📱 WhatsApp (cliente): <a href="$waHref">$maskedPhone</a></p>
            <p style="margin:0 0 4px">📍 Endereço: $addressLine</p>
            <p style="margin:0 0 4px">💳 Método: Cartão de crédito (recusado)</p>
            $noteBlock
        """.trimIndent()

        val footer = CardEmailBase.buildFooter()

        return """
        <html>
        <body style="font-family:Arial,Helvetica,sans-serif;background:#f6f7f9;padding:24px;font-size:14px">
          <div style="max-width:640px;margin:0 auto;background:#fff;border:1px solid #eee;border-radius:12px;overflow:hidden">

            <!-- HEADER -->
            <div style="background:linear-gradient(135deg,#0a2239,#0e4b68);color:#fff;padding:16px 20px;">
              <table width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse">
                <tr>
                  <td style="width:64px;vertical-align:middle;">
                    <img src="$logoUrl" alt="${CardEmailBase.escapeHtml(brandName)}" width="56" style="display:block;border-radius:6px;">
                  </td>
                  <td style="text-align:right;vertical-align:middle;">
                    <div style="font-weight:700;font-size:14px;line-height:1;">${CardEmailBase.escapeHtml(brandName)}</div>
                    <div style="height:6px;line-height:6px;font-size:0;">&nbsp;</div>
                    <div style="opacity:.9;font-size:14px;line-height:1.2;">Pedido recusado no cartão</div>
                  </td>
                </tr>
              </table>
            </div>

            <div style="padding:20px">
              $header

              <p style="margin:12px 0 8px"><strong>🧾 Nº do pedido:</strong> #${CardEmailBase.escapeHtml(order.id.toString())}</p>
            </div>

            $footer
          </div>
        </body>
        </html>
        """.trimIndent()
    }
}