package no.fdk.userapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthorizedPartyRequest(
    val type: String,
    val value: String,
    val partyFilter: List<UrnAttribute>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UrnAttribute(
    val type: String,
    val value: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthorizedParty(
    val partyUuid: String? = null,
    val name: String? = null,
    val organizationNumber: String? = null,
    val personId: String? = null,
    val dateOfBirth: String? = null,
    val type: String? = null,
    val partyId: Int? = null,
    val unitType: String? = null,
    val isDeleted: Boolean = false,
    val onlyHierarchyElementWithNoAccess: Boolean = false,
    val authorizedResources: List<String>? = null,
    val authorizedAccessPackages: List<String>? = null,
    val authorizedRoles: List<String>? = null,
    val authorizedInstances: List<AuthorizedResource>? = null,
    val subunits: List<AuthorizedParty>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthorizedResource(
    val resourceId: String? = null,
    val instanceId: String? = null
)
