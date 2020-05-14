package no.fdk.userapi.mapper

import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.AltinnSubject
import no.fdk.userapi.model.UserFDK

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
        organizationNumber
    )