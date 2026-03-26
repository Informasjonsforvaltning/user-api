package no.fdk.userapi.mapper

import no.fdk.userapi.model.AuthorizedParty
import no.fdk.userapi.model.*

fun List<AuthorizedParty>.toAltinnPerson(ssn: String): AltinnPerson? {
    val person = firstOrNull { it.type == "Person" && it.personId == ssn } ?: return null
    val orgs = filter { it.type == "Organization" && it.organizationNumber != null && it.isDeleted != true }
        .map { it.toAltinnOrganization() }
    return AltinnPerson(person.name, person.personId, orgs)
}

fun AuthorizedParty.toAltinnOrganization(): AltinnOrganization =
    AltinnOrganization(
        name = name,
        organizationForm = unitType,
        organizationNumber = organizationNumber,
        type = AltinnReporteeType.Organization
    )

fun List<AuthorizedParty>.toFDKRoles(ssn: String, orgNumber: String): List<RoleFDK> {
    val org = firstOrNull { it.type == "Organization" && it.organizationNumber == orgNumber } ?: return emptyList()
    return org.authorizedResources.orEmpty()
        .mapNotNull { resourceIdToRole(it)?.let { role -> RoleFDK(RoleFDK.ResourceType.Organization, orgNumber, role) } }
        .distinct()
}

fun AltinnPerson.toUserFDK(): UserFDK? {
    if (socialSecurityNumber == null) return null
    val names = name?.split("\\s+".toRegex())?.toList() ?: emptyList()
    if (names.isEmpty()) return null
    return UserFDK(
        id = socialSecurityNumber!!,
        firstName = names.dropLast(1).joinToString(" "),
        lastName = names.last()
    )
}

fun isPid(username: String): Boolean = username.matches(Regex("^\\d{11}$"))

private fun resourceIdToRole(resourceId: String?): RoleFDK.Role? = when (resourceId) {
    "datanorge-lesetilgang" -> RoleFDK.Role.Read
    "datanorge-skrivetilgang" -> RoleFDK.Role.Write
    "datanorge-virksomhetsadministrator" -> RoleFDK.Role.Admin
    else -> null
}
