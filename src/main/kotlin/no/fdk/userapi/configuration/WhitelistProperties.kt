package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.whitelists")
data class WhitelistProperties (
    val orgNrWhitelist: List<String>,
    val orgFormWhitelist: List<String>,
    val adminList: List<String>
)