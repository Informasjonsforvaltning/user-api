package no.fdk.userapi.adapter

import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.mapper.toAltinnPerson
import no.fdk.userapi.mapper.toFDKRoles
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.RoleFDK
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(AltinnAdapter::class.java)

@Service
class AltinnAdapter(
    private val hostProperties: HostProperties,
    private val accessManagementAdapter: AccessManagementAdapter
) {

    suspend fun getPerson(socialSecurityNumber: String, serviceCode: String): AltinnPerson? {
        if (hostProperties.altinnAccessManagementHost == null) {
            logger.debug("Altinn Access Management host not configured")
            return null
        }
        val parties = accessManagementAdapter.getAuthorizedParties(socialSecurityNumber) ?: return null
        return parties.toAltinnPerson(socialSecurityNumber)
    }

    suspend fun getRights(socialSecurityNumber: String, orgNumber: String): List<RoleFDK>? {
        if (hostProperties.altinnAccessManagementHost == null) {
            logger.debug("Altinn Access Management host not configured")
            return null
        }
        val parties = accessManagementAdapter.getAuthorizedParties(socialSecurityNumber) ?: return null
        return parties.toFDKRoles(socialSecurityNumber, orgNumber)
    }
}
