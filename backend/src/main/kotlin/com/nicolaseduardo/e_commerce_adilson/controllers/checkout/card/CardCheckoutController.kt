package com.nicolaseduardo.e_commerce_adilson.controllers.checkout.card

import CardCheckoutService
import com.nicolaseduardo
import com.nicolaseduardo.e_commerce_adilson.dto.card.CardCheckoutRequest
import com.nicolaseduardo.e_commerce_adilson.dto.card.CardCheckoutResponse

..backend.monolito.dto.card.CardCheckoutRequest
import com.nicolaseduardo..backend.monolito.dto.card.CardCheckoutResponse
import com.nicolaseduardo..backend.monolito.services.card.CardCheckoutService
import com.nicolaseduardo.e_commerce_adilson.web.ApiRoutes
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("${ApiRoutes.API_V1}/checkout/card")
class CardCheckoutController(
    private val cardCheckoutService: CardCheckoutService
) {
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun checkoutCard(@Valid @RequestBody request: CardCheckoutRequest): ResponseEntity<CardCheckoutResponse> =
        ResponseEntity.ok(cardCheckoutService.processCardCheckout(request))
}

//