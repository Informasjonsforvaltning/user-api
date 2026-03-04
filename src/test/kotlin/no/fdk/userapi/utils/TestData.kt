package no.fdk.userapi.utils

import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnReporteeType

const val LOCAL_SERVER_PORT = 5555
const val API_TEST_PORT = 5050

const val EDITOR_ROLE: String = "editor"
const val AUTHOR_ROLE: String = "author"
const val CONTRIBUTOR_ROLE: String = "contributor"
const val SUBSCRIBER_ROLE: String = "subscriber"

const val OK_ADMIN = "ead5e6c2-95ad-4dc7-8172-3ecdb1f0e127"
const val OK_WRITE = "2036c732-f82f-4f40-8b3d-9b678dae6c07"
const val OK_READ = "1f8c1441-dab0-43a5-8984-4e9f12f4e2da"

const val SYS_ADMIN: String = "system:root:admin"
fun orgAdmin(orgNr: String) = "organization:$orgNr:admin"
fun orgWrite(orgNr: String) = "organization:$orgNr:write"
fun orgRead(orgNr: String) = "organization:$orgNr:read"

val ORG: AltinnOrganization = AltinnOrganization(
    name = "Organization Name",
    organizationForm = "ORGL",
    organizationNumber = "920210023",
    type = AltinnReporteeType.Enterprise
)

val ORG_NR_LIST = listOf("920210023", "910258028", "123456789")
val ADMIN_LIST = listOf("23076102252")
val ORG_FORM_LIST = listOf("ADOS", "FKF", "FYLK", "IKS", "KF", "KIRK", "KOMM", "ORGL", "SF", "STAT", "SÆR")

const val SSO_KEY = "ssosecret"

val AUTHORIZED_PARTIES_0 = """
    [
      {
        "partyUuid": "a1b2c3d4-0001-4000-8000-000000000001",
        "name": "KARMSUND OG KYSNESSTRAND REVISJON",
        "organizationNumber": "920210023",
        "personId": null,
        "type": "Organization",
        "partyId": 50001001,
        "unitType": "ORGL",
        "isDeleted": false,
        "onlyHierarchyElementWithNoAccess": false,
        "authorizedResources": ["datanorge-skrivetilgang"]
      },
      {
        "partyUuid": "a1b2c3d4-0002-4000-8000-000000000002",
        "name": "FIRST NAME LAST",
        "organizationNumber": null,
        "personId": "10987654321",
        "type": "Person",
        "partyId": 50001002,
        "unitType": null,
        "isDeleted": false,
        "onlyHierarchyElementWithNoAccess": false,
        "authorizedResources": []
      }
    ]
""".trimIndent()

val AUTHORIZED_PARTIES_1 = """
    [
      {
        "partyUuid": "a1b2c3d4-0003-4000-8000-000000000003",
        "name": "ANOTHER PERSON",
        "organizationNumber": null,
        "personId": "11223344556",
        "type": "Person",
        "partyId": 50001003,
        "unitType": null,
        "isDeleted": false,
        "onlyHierarchyElementWithNoAccess": false,
        "authorizedResources": []
      },
      {
        "partyUuid": "a1b2c3d4-0004-4000-8000-000000000004",
        "name": "RAMSUND OG ROGNAN REVISJON",
        "organizationNumber": "910258028",
        "personId": null,
        "type": "Organization",
        "partyId": 50001004,
        "unitType": "KF",
        "isDeleted": false,
        "onlyHierarchyElementWithNoAccess": false,
        "authorizedResources": ["datanorge-skrivetilgang"]
      },
      {
        "partyUuid": "a1b2c3d4-0005-4000-8000-000000000005",
        "name": "SKATVAL OG BREIVIKBOTN",
        "organizationNumber": "123456789",
        "personId": null,
        "type": "Organization",
        "partyId": 50001005,
        "unitType": "STAT",
        "isDeleted": false,
        "onlyHierarchyElementWithNoAccess": false,
        "authorizedResources": ["datanorge-lesetilgang"]
      }
    ]
""".trimIndent()
