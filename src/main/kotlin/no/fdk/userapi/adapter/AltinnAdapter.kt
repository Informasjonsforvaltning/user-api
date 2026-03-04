package no.fdk.userapi.adapter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
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

    suspend fun getPerson(socialSecurityNumber: String): AltinnPerson? {
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
