package no.fdk.userapi.service

import no.fdk.userapi.configuration.SecurityProperties
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component

@Component
class EndpointPermissions(private val securityProperties: SecurityProperties) {

    fun isFromFDKCluster(httpRequest: ServerHttpRequest): Boolean =
        when (httpRequest.headers["X-API-KEY"]?.first()) {
            null -> false
            securityProperties.ssoApiKey -> true
            else -> false
        }

}
