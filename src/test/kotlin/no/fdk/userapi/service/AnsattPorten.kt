package no.fdk.userapi.service

import no.fdk.userapi.configuration.WhitelistProperties
import no.fdk.userapi.utils.ORG_FORM_LIST
import no.fdk.userapi.utils.orgAdmin
import no.fdk.userapi.utils.orgRead
import no.fdk.userapi.utils.orgWrite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class AnsattPorten {
    private val whitelists: WhitelistProperties = mock()
    private val ansattPortenService = AnsattPortenService(whitelists)

    @BeforeEach
    fun init() {
        whenever(whitelists.orgFormWhitelist).thenReturn(ORG_FORM_LIST)
    }

    private fun encode(json: String): String = Base64.getEncoder().encodeToString(json.toByteArray(Charsets.UTF_8))

    @Nested
    internal inner class Authorities {

        @Test
        fun mapsWhitelistedPartyToWriteRole() {
            val details = encode(
                """
                {
                  "authorized_parties": [{
                    "orgno": {"authority": "iso6523-actorid-upis", "ID": "0192:910244132"},
                    "resource": "datanorge-skrivetilgang",
                    "name": "RAMSUND OG ROGNAN REVISJON",
                    "unit_type": "KF"
                  }],
                  "resource": "urn:altinn:resource:datanorge-skrivetilgang",
                  "type": "ansattporten:altinn:resource",
                  "resource_name": "data.norge.no: Skrivetilgang"
                }
                """.trimIndent(),
            )

            assertEquals(orgWrite("910244132"), ansattPortenService.getAuthorities(details))
        }

        @Test
        fun mapsMultipleResourcesAndFiltersUnknownUnitType() {
            val details = encode(
                """
                [{
                  "authorized_parties": [
                    {
                      "orgno": {"authority": "iso6523-actorid-upis", "ID": "0192:910244132"},
                      "resource": "datanorge-skrivetilgang",
                      "unit_type": "KF"
                    },
                    {
                      "orgno": {"authority": "iso6523-actorid-upis", "ID": "0192:123456789"},
                      "resource": "datanorge-lesetilgang",
                      "unit_type": "STAT"
                    },
                    {
                      "orgno": {"authority": "iso6523-actorid-upis", "ID": "0192:999999999"},
                      "resource": "datanorge-virksomhetsadministrator",
                      "unit_type": "BEDR"
                    }
                  ],
                  "type": "ansattporten:altinn:resource"
                }]
                """.trimIndent(),
            )

            val auth = ansattPortenService.getAuthorities(details)
            assertTrue { auth.contains(orgWrite("910244132")) }
            assertTrue { auth.contains(orgRead("123456789")) }
            assertTrue { !auth.contains(orgAdmin("999999999")) }
        }

        @Test
        fun ignoresUnknownResource() {
            val details = encode(
                """
                {
                  "authorized_parties": [{
                    "orgno": {"authority": "iso6523-actorid-upis", "ID": "0192:910244132"},
                    "resource": "unknown-resource",
                    "unit_type": "KF"
                  }],
                  "type": "ansattporten:altinn:resource"
                }
                """.trimIndent(),
            )

            assertEquals("", ansattPortenService.getAuthorities(details))
        }
    }

    @Nested
    internal inner class OrganizationsForTerms {

        @Test
        fun extractsOrgIdsWithoutWhitelistFilter() {
            val details = encode(
                """
                {
                  "authorized_parties": [
                    {
                      "orgno": {"authority": "iso6523-actorid-upis", "ID": "0192:910244132"},
                      "resource": "datanorge-skrivetilgang",
                      "unit_type": "KF"
                    },
                    {
                      "orgno": {"authority": "iso6523-actorid-upis", "ID": "0192:999999999"},
                      "resource": "datanorge-lesetilgang",
                      "unit_type": "BEDR"
                    }
                  ],
                  "type": "ansattporten:altinn:resource"
                }
                """.trimIndent(),
            )

            assertEquals(listOf("910244132", "999999999"), ansattPortenService.getOrganizationsForTerms(details))
        }
    }
}
