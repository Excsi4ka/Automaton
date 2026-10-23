package dev.excsi.automaton.core.configuration

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetSocketAddress
import java.time.Instant
import java.util.Date
import kotlin.test.assertNull

@WebMvcTest
@Import(SecurityConfiguration::class, SecurityConfigurationTests.TestController::class)
@ActiveProfiles("test")
class SecurityConfigurationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `accepts a signed token for Core without creating a session`() {
        val result = mockMvc.perform(get("/api/security-test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${token()}"))
            .andExpect(status().isOk())
            .andExpect(content().string("secured"))
            .andReturn()

        assertNull(result.request.getSession(false))
    }

    @Test
    fun `rejects missing credentials`() {
        mockMvc.perform(get("/api/security-test"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun `rejects tokens issued for another API`() {
        assertRejected(token(audience = "orchestrator"))
    }

    @Test
    fun `rejects tokens from another issuer`() {
        assertRejected(token(issuer = "https://other.example/realms/other"))
    }

    @Test
    fun `rejects expired tokens`() {
        assertRejected(token(expiresAt = Instant.now().minusSeconds(120)))
    }

    @Test
    fun `rejects tokens signed by an untrusted key`() {
        assertRejected(token(signingKey = RSAKeyGenerator(2048).keyID("test-key").generate()))
    }

    @Test
    fun `does not expose local login or authorization server endpoints`() {
        listOf("/login", "/oauth2/authorize", "/oauth2/jwks").forEach {
            mockMvc.perform(get(it)).andExpect(status().isNotFound())
        }
        mockMvc.perform(post("/oauth2/token")
            .param("grant_type", "client_credentials"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun `denies authenticated requests outside the API`() {
        mockMvc.perform(get("/outside-api")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${token()}"))
            .andExpect(status().isForbidden())
    }

    private fun assertRejected(token: String) {
        mockMvc.perform(get("/api/security-test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
            .andExpect(status().isNotFound())
    }

    private fun token(
        issuer: String = ISSUER,
        audience: String = "core",
        expiresAt: Instant = Instant.now().plusSeconds(300),
        signingKey: RSAKey = key
    ): String {
        val claims = JWTClaimsSet.Builder()
            .issuer(issuer)
            .subject("keycloak-user")
            .audience(audience)
            .issueTime(Date.from(Instant.now().minusSeconds(300)))
            .expirationTime(Date.from(expiresAt))
            .claim("scope", "tasks:read")
            .build()
        val jwt = SignedJWT(JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .keyID(signingKey.keyID)
            .build(), claims)
        jwt.sign(RSASSASigner(signingKey))
        return jwt.serialize()
    }

    @RestController
    class TestController {
        @GetMapping("/api/security-test")
        fun secured(): String = "secured"
    }

    companion object {
        private const val ISSUER = "http://localhost:9998/realms/automaton-test"
        private val key = RSAKeyGenerator(2048).keyID("test-key").generate()
        private val jwksServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/certs") { exchange ->
                val body = JWKSet(key.toPublicJWK()).toString().toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, body.size.toLong())
                exchange.responseBody.use { it.write(body) }
            }
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun jwtProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri") {
                "http://127.0.0.1:${jwksServer.address.port}/certs"
            }
        }

        @JvmStatic
        @AfterAll
        fun stopJwksServer() {
            jwksServer.stop(0)
        }
    }
}
