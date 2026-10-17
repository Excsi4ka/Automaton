package dev.excsi.automaton.core.component

import dev.excsi.automaton.core.configuration.properties.SecurityConfigurationProperties
import dev.excsi.automaton.core.model.User
import dev.excsi.automaton.core.repository.UserRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserInitializer(

    val securityConfigurationProperties: SecurityConfigurationProperties,

    val userRepository: UserRepository,

    val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val initialUser = securityConfigurationProperties.user
        userRepository.findByEmail(initialUser.email)?.let {
            return
        }

        userRepository.save(User(
            email = initialUser.email,
            passwordHash = passwordEncoder.encode(initialUser.password)
        ))
    }
}
