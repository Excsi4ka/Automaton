package dev.excsi.automaton.core.repository

import dev.excsi.automaton.core.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {

    fun findByEmail(email: String): User?
}
