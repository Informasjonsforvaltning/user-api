package no.fdk.userapi.service

import no.fdk.userapi.adapter.AltinnAdapter
import no.fdk.userapi.configuration.WhitelistProperties
import no.fdk.userapi.mapper.toFDKRoles
import no.fdk.userapi.model.*
import no.fdk.userapi.model.RoleFDK.Companion.ROOT_ADMIN
import no.fdk.userapi.model.RoleFDK.ResourceType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(AltinnUserService::class.java)

private const val OLD_SERVICE_CODE = "4814"
private val NEW_SERVICE_CODES = listOf("5755", "5756")

@Service
class AltinnUserService (
    private val whitelists: WhitelistProperties,
    private val altinnAdapter: AltinnAdapter
) {

    fun getUser(id: String): AltinnPerson? {
        // Currently we only fetch one role association from Altinn
        // and we interpret it as publisher admin role in fdk system
        return altinnAdapter.getPerson(id, OLD_SERVICE_CODE)
    }

    fun getAuthorities(id: String): String {
        val resourceRoleTokens: MutableList<String> = mutableListOf()
        if (whitelists.adminList.contains(id)) {
            resourceRoleTokens.add(ROOT_ADMIN.toString())
        }
        resourceRoleTokens.addAll(deprecatedGetPersonAuthorities(id))
        resourceRoleTokens.addAll(getPersonAuthorities(id))

        return resourceRoleTokens.joinToString(",")
    }

    @Deprecated("authorities from old service code")
    private fun deprecatedGetPersonAuthorities(id: String): List<String> {
        val person = altinnAdapter.getPerson(id, OLD_SERVICE_CODE)
        return if(person?.socialSecurityNumber != null) {
            person.organizations
                .filter { org: AltinnOrganization -> org.organizationNumber != null }
                .filter { org: AltinnOrganization ->
                    // Organizations should either have an acceptable organization form or be specifically allowed through orgNrWhitelist
                    whitelists.orgNrWhitelist.contains(org.organizationNumber)
                            || whitelists.orgFormWhitelist.contains(org.organizationForm) }
                .map { org: AltinnOrganization -> RoleFDK(ResourceType.Organization, org.organizationNumber!!, RoleFDK.Role.Admin) }
                .map { obj: RoleFDK -> obj.toString() }
        } else emptyList()
    }

    private fun getPersonAuthorities(id: String): List<String> {
        val resourceRoleTokens: MutableList<String> = mutableListOf()
        NEW_SERVICE_CODES.forEach {
            val person = altinnAdapter.getPerson(id, it)
            if(person?.socialSecurityNumber != null) {
                val roles = person.organizations
                    .filter { org: AltinnOrganization -> org.organizationNumber != null }
                    .filter { org: AltinnOrganization ->
                        // Organizations should either have an acceptable organization form or be specifically allowed through orgNrWhitelist
                        whitelists.orgNrWhitelist.contains(org.organizationNumber)
                                || whitelists.orgFormWhitelist.contains(org.organizationForm) }
                    .map { org: AltinnOrganization -> altinnAdapter.getRights(person.socialSecurityNumber, org.organizationNumber!!) }
                    .flatMap { response -> response?.toFDKRoles() ?: emptyList() }
                    .map { obj: RoleFDK -> obj.toString() }

                resourceRoleTokens.addAll(roles)
            }
        }
        return resourceRoleTokens
    }

}
