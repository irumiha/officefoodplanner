package com.officefoodplanner.domain.oauth2.model

final case class AuthRequest(
    redirect_uri: String,
    scope: String,
    client_id: String,
    prompt: String = "consent",
    response_type: String = "code",
    access_type: String = "offline"
)
final case class AccessRequest(
    code: String,
    redirect_uri: String,
    client_id: String,
    client_secret: String,
    scope: String = "",
    grant_type: String = "authorization_code"
)
final case class AccessResponse(
    access_token: String,
    token_type: String,
    expires_in: Long,
    refresh_token: String
)
final case class RefreshRequest(
    client_secret: String,
    refresh_token: String,
    client_id: String,
    grant_type: String = "refresh_token"
)
final case class RefreshResponse(
    access_token: String,
    token_type: String,
    expires_in: Long
)
