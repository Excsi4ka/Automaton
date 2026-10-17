package dev.excsi.automaton.core.component

import dev.excsi.automaton.core.configuration.properties.SecurityConfigurationProperties
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class ServiceClientInitializer(

    val securityConfigurationProperties: SecurityConfigurationProperties,

    val registeredClientRepository: RegisteredClientRepository,

    val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val serviceClient = securityConfigurationProperties.serviceClient
        registeredClientRepository.findByClientId(serviceClient.clientId)?.let {
            return
        }

        val registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(serviceClient.clientId)
            .clientName("Core service")
            .clientSecret(passwordEncoder.encode(serviceClient.clientSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("tasks:submit")
            .scope("tasks:read")
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(10))
                .build())
            .build()

        registeredClientRepository.save(registeredClient)
    }
}
