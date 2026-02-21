package com.nicolaseduardo.e_commerce_adilson.config.payments

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(EfiPixPayoutProps::class, EfiCardPayoutProps::class)
class PropsConfig