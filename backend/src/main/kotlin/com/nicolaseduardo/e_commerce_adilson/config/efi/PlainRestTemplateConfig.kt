package com.nicolaseduardo.e_commerce_adilson.config.efi

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class PlainRestTemplateConfig {
    @Bean("plainRestTemplate")
    fun plainRestTemplate(): RestTemplate = RestTemplate()
}