package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.brreg")
data class BRREGProperties (
    val orgnr: String,
    val adminGroupID: String,
    val writeGroupID: List<String>
)
