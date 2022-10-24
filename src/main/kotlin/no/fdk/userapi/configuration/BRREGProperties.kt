package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("application.brreg")
data class BRREGProperties (
    val orgnr: String
)
