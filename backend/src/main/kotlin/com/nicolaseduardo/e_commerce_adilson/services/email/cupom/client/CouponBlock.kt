import java.math.BigDecimal

/**
 * Helper para gerar bloco HTML de cupom para emails do CLIENTE
 */
object CouponBlock {

    fun build(couponCode: String, discountAmount: BigDecimal): String {
        val discountFormatted = "R$ %.2f".format(discountAmount.toDouble())
        val escapedCode = escapeHtml(couponCode)

        return """
            <!-- CUPOM APLICADO - CLIENTE -->
            <div style="background:#f8f9fa;border:1px solid #dee2e6;border-radius:8px;padding:16px;margin:16px 0;text-align:center;">
              <div style="color:#28a745;font-size:14px;margin-bottom:8px;">🎯</div>
              <div style="font-weight:700;color:#495057;font-size:14px;margin-bottom:4px;">CUPOM APLICADO</div>
              <div style="font-weight:600;color:#6c757d;font-size:14px;margin-bottom:8px;">Código: $escapedCode</div>
              <div style="font-weight:700;color:#28a745;font-size:14px;">Você economizou $discountFormatted! 💰</div>
            </div>
        """.trimIndent()
    }

    private fun escapeHtml(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}