package no.fdk.userapi.utils

import no.fdk.userapi.model.AltinnOrganization

const val LOCAL_SERVER_PORT = 5000

const val EDITOR_ROLE: String = "editor"
const val AUTHOR_ROLE: String = "author"
const val CONTRIBUTOR_ROLE: String = "contributor"
const val SUBSCRIBER_ROLE: String = "subscriber"

const val SYS_ADMIN: String = "system:root:admin"
fun orgAdmin(orgNr: String) = "organization:$orgNr:admin"
fun orgWrite(orgNr: String) = "organization:$orgNr:write"
fun orgRead(orgNr: String) = "organization:$orgNr:read"

val ORG: AltinnOrganization = AltinnOrganization(
    name = "Organization Name",
    organizationForm = "ORGL",
    organizationNumber = "920210023"
)

val ORG_NR_LIST = listOf("920210023", "910258028", "123456789")
val ADMIN_LIST = listOf("23076102252")
val ORG_FORM_LIST = listOf("ADOS", "FKF", "FYLK", "IKS", "KF", "KIRK", "KOMM", "ORGL", "SF", "STAT", "SÆR")
