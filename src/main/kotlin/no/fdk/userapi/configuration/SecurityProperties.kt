package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.secrets")
data class SecurityProperties (
    val ssoApiKey: String,
    val userApiKey: String
)