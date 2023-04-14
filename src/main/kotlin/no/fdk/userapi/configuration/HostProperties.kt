package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.hosts")
data class HostProperties (
    val altinnProxyHost: String,
    val termsHost: String
)