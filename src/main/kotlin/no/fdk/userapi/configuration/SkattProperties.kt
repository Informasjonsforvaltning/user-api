package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.skatt")
data class SkattProperties (
    val orgnr: String,
    val adminGroupID: String,
    val writeGroupID: String,
    val readGroupID: String
)
