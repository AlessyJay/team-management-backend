package com.example.team_management_backend.Auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

data class AuthPrincipal(val id: UUID, val email: String)

data class RegisterRequest(
    @field:NotBlank val name: String,
    @field:Email @field:NotBlank val email: String,
    @field:Size(min = 8) @field:NotBlank val password: String,
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String,
)

data class RegisterResponse(val message: String, val userId: UUID, val email: String)
data class LoginResponse(val accessToken: String, val userId: UUID, val email: String)
data class RefreshResponse(val accessToken: String)
