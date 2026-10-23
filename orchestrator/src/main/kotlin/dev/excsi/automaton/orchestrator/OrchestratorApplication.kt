package dev.excsi.automaton.orchestrator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class OrchestratorApplication

fun main(args: Array<String>) {
	runApplication<OrchestratorApplication>(*args)
}
