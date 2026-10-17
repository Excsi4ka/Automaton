package dev.excsi.automaton.core.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.orchestrator")
data class OrchestratorConfigurationProperties(

    val url: String,
)
