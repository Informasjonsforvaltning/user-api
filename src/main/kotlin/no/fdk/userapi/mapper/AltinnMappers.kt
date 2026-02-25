package no.fdk.userapi.mapper

import no.fdk.userapi.model.AuthorizedParty
import no.fdk.userapi.model.*

fun List<AuthorizedParty>.toAltinnPerson(ssn: String): AltinnPerson? {
    val person = firstOrNull { it.type == "Person" && it.personId == ssn } ?: return null
    val orgs = filter { it.type == "Organization" && it.organizationNumber != null && !it.isDeleted }
        .map { it.toAltinnOrganization() }
    return AltinnPerson(
        name = person.name,
        socialSecurityNumber = person.personId,
        organizations = orgs
    )
}

fun AuthorizedParty.toAltinnOrganization(): AltinnOrganization =
    AltinnOrganization(
        name = name,
        organizationForm = unitType,
        organizationNumber = organizationNumber,
        type = when (unitType?.uppercase()) {
            "AS", "NU", "BEDR", "KF" -> AltinnReporteeType.Enterprise
            else -> AltinnReporteeType.Business
        }
    )

fun List<AuthorizedParty>.toAltinnRightsResponse(ssn: String, orgNumber: String): AltinnRightsResponse? {
    val person = firstOrNull { it.type == "Person" && it.personId == ssn } ?: return null
    val org = firstOrNull { it.type == "Organization" && it.organizationNumber == orgNumber } ?: return null
    val rights = (org.authorizedResources.orEmpty()).mapNotNull { resourceId ->
        resourceIdToServiceCode(resourceId)?.let { code ->
            AltinnRights(serviceCode = code)
        }
    }
    return AltinnRightsResponse(
        subject = AltinnSubject(name = person.name, type = "Person", socialSecurityNumber = person.personId),
        reportee = AltinnReportee(name = org.name, organizationNumber = org.organizationNumber, type = "Organization"),
        rights = rights
    )
}

private fun resourceIdToServiceCode(resourceId: String?): String? =
    when (resourceId) {
        null -> null
        "datanorge-lesetilgang" -> "5756"
        "datanorge-skrivetilgang" -> "5755"
        "datanorge-virksomhetsadministrator" -> "5977"
        else -> null
    }

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
