package no.fdk.userapi.service

import no.fdk.userapi.adapter.AltinnAdapter
import no.fdk.userapi.configuration.WhitelistProperties
import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.RoleFDK
import no.fdk.userapi.model.RoleFDK.Companion.ROOT_ADMIN
import no.fdk.userapi.model.RoleFDK.ResourceType
import org.springframework.stereotype.Service

@Service
class AltinnUserService (
    private val whitelists: WhitelistProperties,
    private val altinnAdapter: AltinnAdapter
) {

    fun getUser(id: String): AltinnPerson? {
        // Currently we only fetch one role association from Altinn
        // and we interpret it as publisher admin role in fdk system
        return altinnAdapter.getPerson(id)
    }

    fun getAuthorities(id: String): String? {
        return getPersonAuthorities(altinnAdapter.getPerson(id))
    }

    private fun getPersonAuthorities(person: AltinnPerson?): String? {
        if(person?.socialSecurityNumber != null) {
            val resourceRoleTokens: MutableList<String> = person.organizations
                .asSequence()
                .filter { org: AltinnOrganization -> org.organizationNumber != null }
                .filter { org: AltinnOrganization ->
                    // Organizations should either have an acceptable organization form or be specifically allowed through orgNrWhitelist
                    whitelists.orgNrWhitelist.contains(org.organizationNumber)
                        || whitelists.orgFormWhitelist.contains(org.organizationForm) }
                .map { org: AltinnOrganization -> RoleFDK(ResourceType.Organization, org.organizationNumber!!, RoleFDK.Role.Admin) }
                .map { obj: RoleFDK -> obj.toString() }
                .toMutableList()

            if (whitelists.adminList.contains(person.socialSecurityNumber)) {
                resourceRoleTokens.add(ROOT_ADMIN.toString())
            }

            return resourceRoleTokens.joinToString(",")
        } else return null
    }

}