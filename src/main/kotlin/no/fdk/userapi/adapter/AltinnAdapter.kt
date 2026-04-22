package no.fdk.userapi.adapter

import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.mapper.toAltinnPerson
import no.fdk.userapi.mapper.toFDKRoles
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.AuthorizedParty
import no.fdk.userapi.model.RoleFDK
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(AltinnAdapter::class.java)

@Service
class AltinnAdapter(
    private val hostProperties: HostProperties,
    private val accessManagementAdapter: AccessManagementAdapter
) {

    suspend fun getAuthorizedParties(socialSecurityNumber: String): List<AuthorizedParty>? {
        if (hostProperties.altinnAccessManagementHost == null) {
            logger.debug("Altinn Access Management host not configured")
            return null
        }
        return accessManagementAdapter.getAuthorizedParties(socialSecurityNumber)
    }

    suspend fun getPerson(socialSecurityNumber: String): AltinnPerson? =
        getAuthorizedParties(socialSecurityNumber)?.toAltinnPerson(socialSecurityNumber)

    suspend fun getRights(socialSecurityNumber: String, orgNumber: String): List<RoleFDK>? =
        getAuthorizedParties(socialSecurityNumber)?.toFDKRoles(socialSecurityNumber, orgNumber)
}
