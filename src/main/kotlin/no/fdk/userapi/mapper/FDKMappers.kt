package no.fdk.userapi.mapper

import no.fdk.userapi.model.RoleFDK

fun fdkRoleFromAuthString(authString: String): RoleFDK {
    val authParts: List<String> = authString.split(":")

    return if (authParts.size != 3) RoleFDK.INVALID
    else {
        RoleFDK(
            authParts[0].fdkRoleResourceType(),
            authParts[1],
            authParts[2].fdkRoleType()
        )
    }
}

private fun String.fdkRoleResourceType(): RoleFDK.ResourceType =
    when(this) {
        RoleFDK.ResourceType.System.value -> RoleFDK.ResourceType.System
        RoleFDK.ResourceType.Organization.value -> RoleFDK.ResourceType.Organization
        else -> RoleFDK.ResourceType.Invalid
    }

private fun String.fdkRoleType(): RoleFDK.Role =
    when(this) {
        RoleFDK.Role.Admin.value -> RoleFDK.Role.Admin
        RoleFDK.Role.Read.value -> RoleFDK.Role.Read
        else -> RoleFDK.Role.Invalid
    }
