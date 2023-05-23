package no.fdk.userapi.service

import no.fdk.userapi.configuration.BRREGProperties
import no.fdk.userapi.model.RoleFDK
import org.springframework.stereotype.Service

@Service
class BRREGService(
    private val brregProperties: BRREGProperties
) {

    fun getAuthorities(groups: List<String>): String {
        val role = when {
            groups.contains(brregProperties.adminGroupID) -> RoleFDK.Role.Admin
            groups.contains(brregProperties.writeGroupID) -> RoleFDK.Role.Write
            else -> throw Exception("unauthorized brreg login")
        }
        return RoleFDK(
            RoleFDK.ResourceType.Organization,
            brregProperties.orgnr,
            role
        ).toString()
    }

}
