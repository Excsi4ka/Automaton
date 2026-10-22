package dev.excsi.automaton.orchestrator.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "workflows")
class Workflow(

    @field:Id
    var id: UUID = UUID.randomUUID(),

    @field:Column(name = "name")
    var name: String,

    @field:Column(name = "created_at")
    var createdAt: Instant = Instant.now(),
)