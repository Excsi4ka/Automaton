package dev.excsi.automaton.orchestrator.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "jobs")
class Job {

    @field:Id
    var id: UUID = UUID.randomUUID()
}