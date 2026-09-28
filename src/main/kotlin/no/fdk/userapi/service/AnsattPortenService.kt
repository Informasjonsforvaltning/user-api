package no.fdk.userapi.service

import no.fdk.userapi.configuration.WhitelistProperties
import no.fdk.userapi.mapper.orgId
import no.fdk.userapi.mapper.parseAnsattPortenParties
import no.fdk.userapi.mapper.resourceIdToRole
import no.fdk.userapi.model.RoleFDK
import org.springframework.stereotype.Service

@Service
class AnsattPortenService(private val whitelists: WhitelistProperties) {

    fun getAuthorities(details: String): String = parseAnsattPortenParties(details)
        .filter { party -> party.unitType != null && whitelists.orgFormWhitelist.contains(party.unitType) }
        .mapNotNull { party ->
            val orgId = party.orgId() ?: return@mapNotNull null
            val role = resourceIdToRole(party.resource) ?: return@mapNotNull null
            RoleFDK(RoleFDK.ResourceType.Organization, orgId, role)
        }
        .distinct()
        .joinToString(",")

    fun getOrganizationsForTerms(details: String): List<String> = parseAnsattPortenParties(details)
        .mapNotNull { it.orgId() }
        .distinct()
}
