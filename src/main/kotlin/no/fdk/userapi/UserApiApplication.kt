package no.fdk.userapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.configuration.SecurityProperties
import no.fdk.userapi.configuration.WhitelistProperties
import org.springframework.boot.SpringApplication

@SpringBootApplication
@EnableConfigurationProperties(HostProperties::class, SecurityProperties::class, WhitelistProperties::class)
open class UserApiApplication

fun main(args: Array<String>) {
    SpringApplication.run(UserApiApplication::class.java, *args)
}
