package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.hosts")
data class HostProperties (
    val termsHost: String,
    val maskinportenApiHost: String? = null,
    val maskinportenScope: String? = null,
    val altinnAccessManagementHost: String? = null
)