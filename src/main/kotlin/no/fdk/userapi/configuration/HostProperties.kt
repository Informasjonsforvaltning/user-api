package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("application.hosts")
data class HostProperties (
    val altinnProxyHost: String
)