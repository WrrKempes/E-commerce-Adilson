import java.math.BigDecimal

interface PixPayoutProvider {
    fun sendPixPayout(orderId: Long, amount: BigDecimal, favoredPixKey: String): String
}