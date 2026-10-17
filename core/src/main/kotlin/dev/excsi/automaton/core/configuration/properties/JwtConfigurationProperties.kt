package dev.excsi.automaton.core.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.jwt")
data class JwtConfigurationProperties(

    val publicKey: String,

    val privateKey: String,

    val keyId: String
)