package dev.excsi.automaton.core.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.NOT_FOUND)

        return http
            .authorizeHttpRequests {
                it.requestMatchers("/error").permitAll()
                it.requestMatchers("/api/**").authenticated()
                it.anyRequest().denyAll()
            }
            // Authentication uses explicit bearer headers rather than browser cookies.
            .csrf { it.disable() }
            .requestCache { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
            }
            .oauth2ResourceServer {
                it.jwt {}
                it.authenticationEntryPoint(authenticationEntryPoint)
            }
            .build()
    }
}
