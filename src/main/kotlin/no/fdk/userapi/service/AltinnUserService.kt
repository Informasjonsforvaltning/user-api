package no.fdk.userapi.service

import no.fdk.userapi.adapter.AltinnAdapter
import no.fdk.userapi.configuration.WhitelistProperties
import no.fdk.userapi.mapper.toAltinnPerson
import no.fdk.userapi.mapper.toFDKRoles
import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.RoleFDK.Companion.ROOT_ADMIN
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(AltinnUserService::class.java)

@Service
class AltinnUserService (
    private val whitelists: WhitelistProperties,
    private val altinnAdapter: AltinnAdapter
) {

    suspend fun getUser(id: String): AltinnPerson? =
        altinnAdapter.getPerson(id)

    fun getSysAdminAuthorities(id: String): List<String> =
        if (whitelists.adminList.contains(id)) listOf(ROOT_ADMIN.toString())
        else emptyList()

    suspend fun organizationsForService(ssn: String): List<AltinnOrganization> {
        val person = altinnAdapter.getPerson(ssn)
        return organizationsMatchingServiceWhitelists(person)
    }

    suspend fun authForOrganization(ssn: String, org: AltinnOrganization): List<String> =
        (altinnAdapter.getRights(ssn, org.organizationNumber!!) ?: emptyList()).map { it.toString() }

    private fun isWhitelistedOrgNumber(org: AltinnOrganization) =
        org.organizationNumber?.let { whitelists.orgNrWhitelist.contains(it) } ?: false

    private fun isWhitelistedOrgForm(org: AltinnOrganization) =
        org.organizationForm != null && whitelists.orgFormWhitelist.contains(org.organizationForm)

    suspend fun getAuthorities(ssn: String): String {
        val resourceRoleTokens: MutableList<String> = mutableListOf()
        val authTasks = listOf(
            organizationAuthorities(ssn),
            getSysAdminAuthorities(ssn)
        )
        logger.debug("Getting authorities, running coroutines")
        authTasks.forEach { resourceRoleTokens.addAll(it) }
        return resourceRoleTokens.distinct().joinToString(",")
    }

    private suspend fun organizationAuthorities(ssn: String): List<String> {
        val parties = altinnAdapter.getAuthorizedParties(ssn) ?: return emptyList()
        val person = parties.toAltinnPerson(ssn) ?: return emptyList()
        if (person.socialSecurityNumber == null) return emptyList()
        val orgs = organizationsMatchingServiceWhitelists(person)
        logger.debug("Getting organization authorities from single authorized-parties response")
        return orgs.flatMap { org ->
            parties.toFDKRoles(ssn, org.organizationNumber!!).orEmpty().map { it.toString() }
        }
    }

    private fun organizationsMatchingServiceWhitelists(person: AltinnPerson?): List<AltinnOrganization> =
        if (person?.socialSecurityNumber != null) {
            person.organizations
                .filter { org: AltinnOrganization -> org.organizationNumber != null }
                .filter { org: AltinnOrganization ->
                    isWhitelistedOrgNumber(org) || isWhitelistedOrgForm(org)
                }
        } else emptyList()

    suspend fun getOrganizationsForTerms(ssn: String): List<AltinnOrganization> {
        logger.debug("Getting organizations for terms, running coroutines")
        return getUser(ssn)?.organizations ?: emptyList()
    }
}
