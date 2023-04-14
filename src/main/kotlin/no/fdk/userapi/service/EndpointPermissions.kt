package no.fdk.userapi.service

import jakarta.servlet.http.HttpServletRequest
import no.fdk.userapi.configuration.SecurityProperties
import org.springframework.stereotype.Component

@Component
class EndpointPermissions(private val securityProperties: SecurityProperties) {

    fun isFromFDKCluster(httpServletRequest: HttpServletRequest): Boolean =
        when (httpServletRequest.getHeader("X-API-KEY")) {
            null -> false
            securityProperties.ssoApiKey -> true
            else -> false
        }

}
