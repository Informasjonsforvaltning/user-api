package no.fdk.userapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnsattPortenDetails(
    @JsonProperty("authorized_parties")
    val authorizedParties: List<AnsattPortenParty>? = null,
    val resource: String? = null,
    val type: String? = null,
    @JsonProperty("resource_name")
    val resourceName: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnsattPortenParty(
    val orgno: AnsattPortenOrgno? = null,
    val resource: String? = null,
    val name: String? = null,
    @JsonProperty("unit_type")
    val unitType: String? = null,
    val actions: List<String>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnsattPortenOrgno(
    val authority: String? = null,
    @JsonProperty("ID")
    val id: String? = null,
)
