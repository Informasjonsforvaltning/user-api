package no.fdk.userapi.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.*

@ConstructorBinding
@ConfigurationProperties("application.whitelists")
class WhitelistProperties (
    private val orgNrWhitelist: String,
    private val orgFormWhitelist: String,
    private val adminList: String
) {

    fun getOrgNrWhitelist(): List<String> =
        orgNrWhitelist.split(",").toList()

    fun getOrgFormWhitelist(): List<String> =
        orgFormWhitelist.split(",").toList()

    fun getAdminList(): List<String> =
        adminList.split(",").toList()

}