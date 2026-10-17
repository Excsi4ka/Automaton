## Core Service

The Core Service is the central, public-facing entry point of the distributed automation platform. Built with Spring Boot, it handles authentication, authorization, user management, and common platform-level concerns.

It serves as the API gateway for client requests, incoming webhooks, and external integrations, securely communicating with internal services such as the Workflow Orchestrator over authenticated HTTP.

The Core Service does not execute workflows or jobs directly. Instead, it validates and routes requests to the appropriate internal services, providing a consistent and secure interface to the platform.
