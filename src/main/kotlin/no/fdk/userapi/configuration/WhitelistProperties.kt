package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.*

@ConstructorBinding
@ConfigurationProperties("application.whitelists")
data class WhitelistProperties (
    val orgNrWhitelist: List<String>,
    val orgFormWhitelist: List<String>,
    val adminList: List<String>
)