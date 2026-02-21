package com.nicolaseduardo.e_commerce_adilson.controllers.checkout.pix

import com.nicolaseduardo.backend.monolito.dto.pix.PixCheckoutRequest
import com.nicolaseduardo.backend.monolito.dto.pix.PixCheckoutResponse
import com.nicolaseduardo.backend.monolito.services.pix.PixCheckoutService
import com.nicolaseduardo.e_commerce_adilson.dto.pix.PixCheckoutRequest
import com.nicolaseduardo.e_commerce_adilson.dto.pix.PixCheckoutResponse
import com.nicolaseduardo.e_commerce_adilson.web.ApiRoutes
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiRoutes.API_V1}/checkout/pix")
class PixCheckoutController(
    private val checkoutService: PixCheckoutService
) {
    @PostMapping
    fun checkoutPix(@RequestBody request: PixCheckoutRequest): ResponseEntity<PixCheckoutResponse> =
        ResponseEntity.ok(checkoutService.processCheckout(request))
}