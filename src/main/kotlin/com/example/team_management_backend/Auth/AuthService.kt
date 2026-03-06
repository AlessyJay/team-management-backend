package com.example.team_management_backend.Auth

import org.springframework.stereotype.Service
import com.example.team_management_backend.Entities.User
import com.example.team_management_backend.Auth.AuthRequest
import com.example.team_management_backend.Auth.AuthResponse

@Service
class AuthService(private val authRepo: AuthRepository) {
    fun registerUser(user: AuthRequest): AuthResponse {
        val newUser = User(
            name = user.name,
            email = user.email,
            password = user.password,
        )

        return authRepo.save(newUser).toResponse()
    }

    private fun User.toResponse() = AuthResponse(
        id = id.toString(),
        name = this.name,
        email = this.email,
        joinedAt = this.joinedAt,
    )
}