package dev.excsi.automaton.core.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.security")
data class SecurityConfigurationProperties(

    val issuer: String,

    val audience: String,

    val user: InitialUser,

    val serviceClient: ServiceClient
)

data class InitialUser(

    val email: String,
    val password: String
)

data class ServiceClient(

    val clientId: String,

    val clientSecret: String,

    val audience: String
)
