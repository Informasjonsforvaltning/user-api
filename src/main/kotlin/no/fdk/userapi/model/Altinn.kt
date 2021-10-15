package no.fdk.userapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnOrganization (
    val name: String?,
    val organizationForm: String?,
    val organizationNumber: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnReportee (
    @JsonProperty("Name")
    val name: String? = null,
    @JsonProperty("OrganizationNumber")
    val organizationNumber: String? = null,
    @JsonProperty("Type")
    val type: String? = null,
    @JsonProperty("Status")
    val status: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnRightsResponse (
    @JsonProperty("Subject")
    val subject: AltinnSubject,
    @JsonProperty("Reportee")
    val reportee: AltinnReportee,
    @JsonProperty("Rights")
    val rights: List<AltinnRights>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AltinnRights (
    @JsonProperty("RightID")
    val id: String? = null,
    @JsonProperty("RightType")
    val type: String? = null,
    @JsonProperty("ServiceCode")
    val serviceCode: String? = null,
    @JsonProperty("ServiceEditionCode")
    val serviceEditionCode: String? = null
)
