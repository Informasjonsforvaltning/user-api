package no.fdk.userapi.service

import no.fdk.userapi.configuration.SecurityProperties
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class EndpointPermissions(private val securityProperties: SecurityProperties) {

    fun isFromFDKCluster(httpServletRequest: HttpServletRequest): Boolean =
        when (httpServletRequest.getHeader("X-API-KEY")) {
            null -> false
            securityProperties.ssoApiKey -> true
            else -> false
        }

}