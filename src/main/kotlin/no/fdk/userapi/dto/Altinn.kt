package no.fdk.userapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

class AltinnOrganization (
    private val subject: AltinnSubject
) {
    val name: String?
        get() = subject.name

    val organizationForm: String?
        get() = subject.organizationForm

    val organizationNumber: String?
        get() = subject.organizationNumber

}

class AltinnPerson (
    private val subject: AltinnSubject,
    val organizations: List<AltinnOrganization>
) {
    val name: String?
        get() = subject.name

    val socialSecurityNumber: String?
        get() = subject.socialSecurityNumber

}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AltinnSubject (
    @JsonProperty("Name")
    val name: String? = null,

    @JsonProperty("Type")
    val type: String? = null,

    @JsonProperty("OrganizationNumber")
    val organizationNumber: String? = null,

    @JsonProperty("OrganizationForm")
    val organizationForm: String? = null,

    @JsonProperty("Status")
    val status: String? = null,

    @JsonProperty("SocialSecurityNumber")
    val socialSecurityNumber: String? = null
)