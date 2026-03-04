package no.fdk.userapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnOrganization (
    val name: String?,
    val organizationForm: String?,
    val organizationNumber: String?,
    val type: AltinnReporteeType?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnPerson (
    val name: String?,
    val socialSecurityNumber: String?,
    val organizations: List<AltinnOrganization>
)

enum class AltinnReporteeType {
    Business, Enterprise, Person
}
