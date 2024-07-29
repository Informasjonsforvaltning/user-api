package no.fdk.userapi.service

import no.fdk.userapi.adapter.AltinnAdapter
import no.fdk.userapi.configuration.WhitelistProperties
import no.fdk.userapi.mapper.toFDKRoles
import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.AltinnReporteeType
import no.fdk.userapi.model.RoleFDK
import no.fdk.userapi.model.RoleFDK.Companion.ROOT_ADMIN
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(AltinnUserService::class.java)
val SERVICE_CODES = listOf("5977", "5755", "5756")

@Service
class AltinnUserService (
    private val whitelists: WhitelistProperties,
    private val altinnAdapter: AltinnAdapter
) {

    suspend fun getUser(id: String, serviceCode: String): AltinnPerson? =
        altinnAdapter.getPerson(id, serviceCode)

    fun getSysAdminAuthorities(id: String): List<String> =
        if (whitelists.adminList.contains(id)) listOf(ROOT_ADMIN.toString())
        else emptyList()

    suspend fun organizationsForService(ssn: String, serviceCode: String): List<AltinnOrganization> {
        val person = altinnAdapter.getPerson(ssn, serviceCode)
        return if(person?.socialSecurityNumber != null) {
            person.organizations
                .filter { org: AltinnOrganization -> org.organizationNumber != null }
                .filter { org: AltinnOrganization ->
                    // Organizations should either have an acceptable organization form
                    // or be specifically allowed through orgNrWhitelist
                    isWhitelistedOrgNumber(org) || isWhitelistedOrgForm(org)
                }
        } else emptyList()
    }

    suspend fun authForOrganization(ssn: String, org: AltinnOrganization): List<String> =
        altinnAdapter.getRights(ssn, org.organizationNumber!!)
            .let { response -> response?.toFDKRoles() ?: emptyList() }
            .map { obj: RoleFDK -> obj.toString() }

    private fun isWhitelistedOrgNumber(org: AltinnOrganization) =
        org.organizationNumber?.let { whitelists.orgNrWhitelist.contains(it) } ?: false

    private fun isWhitelistedOrgForm(org: AltinnOrganization) =
        when {
            org.type != AltinnReporteeType.Enterprise -> false
            org.organizationForm == null -> false
            whitelists.orgFormWhitelist.contains(org.organizationForm) -> true
            else -> false
        }

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

    private suspend fun allOrganizations(ssn: String): List<AltinnOrganization> {
        val orgTasks = SERVICE_CODES.map {
            organizationsForService(ssn, it)
        }
        logger.debug("Getting all organizations, running coroutines")
        return orgTasks.flatten()
    }

    private suspend fun organizationAuthorities(ssn: String): List<String> {
        val rightsTasks = allOrganizations(ssn).map {
            authForOrganization(ssn, it)
        }
        logger.debug("Getting organization authorities, running coroutines")
        return rightsTasks.flatten()
    }

    suspend fun getOrganizationsForTerms(ssn: String): List<AltinnOrganization> {
        val getUserTasks = SERVICE_CODES.map { getUser(ssn, it) }
        logger.debug("Getting organizations for terms, running coroutines")
        return getUserTasks.flatMap { it?.organizations ?: emptyList() }
    }
}
