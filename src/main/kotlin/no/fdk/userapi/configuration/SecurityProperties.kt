package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConstructorBinding
@ConfigurationProperties("application.secrets")
data class SecurityProperties (
    val ssoApiKey: String,
    val userApiKey: String
)