package no.fdk.userapi

import no.fdk.userapi.configuration.BRREGProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.configuration.SecurityProperties
import no.fdk.userapi.configuration.WhitelistProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
open class UserApiApplication

fun main(args: Array<String>) {
    SpringApplication.run(UserApiApplication::class.java, *args)
}
