package com.example.team_management_backend.Auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.example.team_management_backend.Entities.User

@Repository
interface AuthRepository : JpaRepository<User, Long> {
    fun findByName(name: String): List<User>
    fun findByEmailContainingIgnoreCase(email: String): List<User>
}