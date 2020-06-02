package no.fdk.userapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class AltinnOrganization (
    val name: String?,
    val organizationForm: String?,
    val organizationNumber: String?
)

data class AltinnPerson (
    val name: String?,
    val socialSecurityNumber: String?,
    val organizations: List<AltinnOrganization>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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