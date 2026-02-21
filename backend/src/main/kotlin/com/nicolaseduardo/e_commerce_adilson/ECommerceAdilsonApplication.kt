package com.nicolaseduardo.e_commerce_adilson

import com.nicolaseduardo.e_commerce_adilson.bootstrap.BootstrapAuthorProperties
import com.nicolaseduardo.e_commerce_adilson.config.coupon.CouponLancamentoProperties
import com.nicolaseduardo.e_commerce_adilson.config.coupon.CouponProperties
import com.nicolaseduardo.e_commerce_adilson.config.efi.CardEfiProperties
import com.nicolaseduardo.e_commerce_adilson.config.efi.PixEfiProperties
import com.nicolaseduardo.e_commerce_adilson.config.payments.EfiCardPayoutProps
import com.nicolaseduardo.e_commerce_adilson.config.payments.EfiPixPayoutProps
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableConfigurationProperties(PrivacyProps::class, PixEfiProperties::class, CardEfiProperties::class, EfiPixPayoutProps::class, EfiCardPayoutProps::class, BootstrapAuthorProperties::class, CouponProperties::class, CouponLancamentoProperties::class)
class BackendMonolitoApplication

class ECommerceAdilsonApplication
fun main(args: Array<String>) {
	runApplication<ECommerceAdilsonApplication>(*args)
}
