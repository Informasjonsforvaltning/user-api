package no.fdk.userapi.service

import no.fdk.userapi.configuration.SkattProperties
import no.fdk.userapi.model.RoleFDK
import org.springframework.stereotype.Service

@Service
class SkattService(
    private val skattProperties: SkattProperties
) {

    fun getAuthorities(groups: List<String>): String {
        val role = when {
            groups.contains(skattProperties.adminGroupID) -> RoleFDK.Role.Admin
            groups.contains(skattProperties.writeGroupID) -> RoleFDK.Role.Write
            groups.contains(skattProperties.readGroupID) -> RoleFDK.Role.Read
            else -> throw Exception("unauthorized skatt login")
        }
        return RoleFDK(
            RoleFDK.ResourceType.Organization,
            skattProperties.orgnr,
            role
        ).toString()
    }

}
