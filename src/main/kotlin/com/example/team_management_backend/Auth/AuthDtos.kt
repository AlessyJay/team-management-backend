package com.example.team_management_backend.Auth

import java.time.OffsetDateTime

data class AuthRequest(
    var name: String,
    var email: String,
    var password: String,
)

data class AuthResponse(
    var id: String,
    var name: String,
    var email: String,
    var joinedAt: OffsetDateTime,
)