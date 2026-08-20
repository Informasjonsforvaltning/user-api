package no.fdk.userapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenResponse(
    @param:JsonProperty("access_token")
    val accessToken: String,

    @param:JsonProperty("token_type")
    val tokenType: String = "Bearer",

    @param:JsonProperty("expires_in")
    val expiresIn: Int? = null,

    @param:JsonProperty("scope")
    val scope: String? = null,
)
