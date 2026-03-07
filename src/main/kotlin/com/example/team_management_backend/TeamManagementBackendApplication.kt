package com.example.team_management_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class TeamManagementBackendApplication

fun main(args: Array<String>) {
    runApplication<TeamManagementBackendApplication>(*args)
}
