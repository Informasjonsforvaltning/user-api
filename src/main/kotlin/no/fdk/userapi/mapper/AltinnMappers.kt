package no.fdk.userapi.mapper

import no.fdk.userapi.model.*

fun AltinnPerson.toUserFDK(): UserFDK? {
    val names: List<String> = name
        ?.split("\\s+".toRegex())
        ?.toList()
        ?: emptyList()

    return if (socialSecurityNumber != null) {
        UserFDK(
            id = socialSecurityNumber,
            firstName = names.subList(0, names.size - 1).joinToString(" "),
            lastName = names.last()
        )
    } else null
}

fun AltinnSubject.toPerson(organizations: List<AltinnOrganization>): AltinnPerson =
    AltinnPerson(
        name,
        socialSecurityNumber,
        organizations
    )

fun AltinnSubject.toOrganization(): AltinnOrganization =
    AltinnOrganization(
        name,
        organizationForm,
        organizationNumber,
        type = typeStringToOrgType(type)
    )

fun isPid(username: String): Boolean =
    username.matches("^\\d{11}$".toRegex())

fun AltinnRightsResponse.toFDKRoles(): List<RoleFDK> =
    if (reportee.organizationNumber != null) {
        rights.mapNotNull {
            serviceCodeToRole(it.serviceCode)
                ?.let { role -> RoleFDK(RoleFDK.ResourceType.Organization, reportee.organizationNumber, role) }
        }.distinct()
    } else emptyList()

private fun serviceCodeToRole(serviceCode: String?): RoleFDK.Role? =
    when(serviceCode) {
        null -> null
        "5977" -> RoleFDK.Role.Admin
        "5755" -> RoleFDK.Role.Write
        "5756" -> RoleFDK.Role.Read
        else -> null
    }

private fun typeStringToOrgType(type: String?): AltinnReporteeType? =
    when(type) {
        null -> null
        AltinnReporteeType.Enterprise.name -> AltinnReporteeType.Enterprise
        AltinnReporteeType.Business.name -> AltinnReporteeType.Business
        else -> null
    }
