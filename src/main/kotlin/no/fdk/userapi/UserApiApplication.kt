package no.fdk.userapi

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class UserApiApplication

fun main(args: Array<String>) {
    SpringApplication.run(UserApiApplication::class.java, *args)
}
