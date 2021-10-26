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

@Service
class AltinnUserService (
    private val whitelists: WhitelistProperties,
    private val altinnAdapter: AltinnAdapter
) {

    fun getUser(id: String, serviceCode: String): AltinnPerson? =
        altinnAdapter.getPerson(id, serviceCode)

    @Deprecated("user from old service code")
    fun deprecatedGetUser(id: String): AltinnPerson? {
        return altinnAdapter.getPerson(id, OLD_SERVICE_CODE)
    }

    @Deprecated("authorities from old service code")
    fun deprecatedGetPersonAuthorities(id: String): List<String> {
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

    fun getSysAdminAuthorities(id: String): List<String> =
        if (whitelists.adminList.contains(id)) listOf(ROOT_ADMIN.toString())
        else emptyList()

    fun organizationsForService(ssn: String, serviceCode: String): List<AltinnOrganization> {
        val person = altinnAdapter.getPerson(ssn, serviceCode)
        return if(person?.socialSecurityNumber != null) {
            person.organizations
                .filter { org: AltinnOrganization -> org.organizationNumber != null }
                .filter { org: AltinnOrganization ->
                    // Organizations should either have an acceptable organization form or be specifically allowed through orgNrWhitelist
                    whitelists.orgNrWhitelist.contains(org.organizationNumber)
                            || whitelists.orgFormWhitelist.contains(org.organizationForm)
                }
        } else emptyList()
    }

    fun authForOrganization(ssn: String, org: AltinnOrganization): List<String> =
        altinnAdapter.getRights(ssn, org.organizationNumber!!)
            .let { response -> response?.toFDKRoles() ?: emptyList() }
            .map { obj: RoleFDK -> obj.toString() }

}
