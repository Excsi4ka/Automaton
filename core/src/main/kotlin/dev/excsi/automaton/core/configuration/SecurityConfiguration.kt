package dev.excsi.automaton.core.configuration

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import dev.excsi.automaton.core.configuration.properties.JwtConfigurationProperties
import dev.excsi.automaton.core.configuration.properties.SecurityConfigurationProperties
import dev.excsi.automaton.core.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwtAudienceValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Configuration
@EnableWebSecurity
class SecurityConfiguration(

    val jwtConfigurationProperties: JwtConfigurationProperties,

    val securityConfigurationProperties: SecurityConfigurationProperties
) {

    @Bean
    @Order(1)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .securityMatcher("/api/**")
            .authorizeHttpRequests {
                it.anyRequest().authenticated()
            }
            // API authentication uses an explicit bearer header, not browser cookies.
            .csrf { it.disable() }
            .requestCache { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .oauth2ResourceServer { it.jwt {} }
            .build()
    }

    @Bean
    @Order(2)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .oauth2AuthorizationServer {}
            .authorizeHttpRequests {
                it.requestMatchers("/error").permitAll()
                it.anyRequest().authenticated()
            }
            .formLogin { it.permitAll() }
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService {
        return UserDetailsService {
            val user = userRepository.findByEmail(it)
                ?: throw UsernameNotFoundException("User not found")

            User.withUsername(user.email)
                .password(user.passwordHash)
                .authorities(emptyList())
                .build()
        }
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val decoder = NimbusJwtDecoder.withPublicKey(getPublicKey())
            .signatureAlgorithm(SignatureAlgorithm.RS256)
            .build()

        decoder.setJwtValidator(DelegatingOAuth2TokenValidator(
            JwtValidators.createDefaultWithIssuer(securityConfigurationProperties.issuer),
            JwtAudienceValidator(securityConfigurationProperties.audience)
        ))

        return decoder
    }

    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val rsaKey = RSAKey.Builder(getPublicKey())
            .privateKey(getPrivateKey())
            .keyID(jwtConfigurationProperties.keyId)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .build()

        return ImmutableJWKSet(JWKSet(rsaKey))
    }

    private fun getPublicKey(): RSAPublicKey {
        val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(jwtConfigurationProperties.publicKey))
        return KeyFactory.getInstance("RSA").generatePublic(keySpec) as RSAPublicKey
    }

    private fun getPrivateKey(): RSAPrivateKey {
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(jwtConfigurationProperties.privateKey))
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec) as RSAPrivateKey
    }
}
