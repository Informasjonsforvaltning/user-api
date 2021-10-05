package no.fdk.userapi.mapper

import no.fdk.userapi.model.RoleFDK
import no.fdk.userapi.model.RoleFDK.Companion.ROOT_ADMIN

private const val EDITOR = "editor"
private const val AUTHOR = "author"
private const val CONTRIBUTOR = "contributor"
private const val SUBSCRIBER = "subscriber"

fun mapAuthoritiesFromDifiRole(roles: List<String>, orgs: List<String>): String {
    var orgIndex = 0

    val fdkRoles = roles.mapNotNull {
        when(it) {
            EDITOR -> ROOT_ADMIN
            AUTHOR ->  {
                if (orgIndex < orgs.size) {
                    val orgId = orgs[orgIndex]
                    orgIndex++
                    RoleFDK(RoleFDK.ResourceType.Organization, orgId, RoleFDK.Role.Admin)
                } else null
            }
            CONTRIBUTOR -> {
                if (orgIndex < orgs.size) {
                    val orgId = orgs[orgIndex]
                    orgIndex++
                    RoleFDK(RoleFDK.ResourceType.Organization, orgId, RoleFDK.Role.Write)
                } else null
            }
            SUBSCRIBER ->  {
                if (orgIndex < orgs.size) {
                    val orgId = orgs[orgIndex]
                    orgIndex++
                    RoleFDK(RoleFDK.ResourceType.Organization, orgId, RoleFDK.Role.Read)
                } else null
            }
            else -> null
        }
    }

    return fdkRoles.joinToString(",")
}
