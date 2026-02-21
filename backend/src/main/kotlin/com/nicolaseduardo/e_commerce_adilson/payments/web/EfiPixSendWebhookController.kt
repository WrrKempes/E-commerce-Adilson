package com.nicolaseduardo.e_commerce_adilson.payments.web

import java.math.BigDecimal

interface PixPayoutProvider {
    fun sendPixPayout(orderId: Long, amount: BigDecimal, favoredPixKey: String): String
}