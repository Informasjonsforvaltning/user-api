package no.fdk.userapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("token_type")
    val tokenType: String = "Bearer",

    @JsonProperty("expires_in")
    val expiresIn: Int? = null,

    @JsonProperty("scope")
    val scope: String? = null
)
