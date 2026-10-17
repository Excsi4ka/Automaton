package dev.excsi.automaton.core.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class User(

    @field:Id
    var id: UUID = UUID.randomUUID(),

    @field:Column(name = "email", unique = true, nullable = false)
    var email: String,

    @field:Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @field:Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
