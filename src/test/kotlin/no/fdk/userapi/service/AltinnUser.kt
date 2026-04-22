package no.fdk.userapi.service

import kotlinx.coroutines.test.runTest
import no.fdk.userapi.adapter.AltinnAdapter
import no.fdk.userapi.configuration.WhitelistProperties
import no.fdk.userapi.model.*
import no.fdk.userapi.utils.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun authorizedPartiesForPerson(
    ssn: String,
    personName: String = "First Last",
    organizationsWithResources: List<Pair<AltinnOrganization, List<String>>> = emptyList()
): List<AuthorizedParty> {
    val person = AuthorizedParty(name = personName, personId = ssn, type = "Person")
    val orgs = organizationsWithResources.map { (org, resources) ->
        AuthorizedParty(
            name = org.name,
            organizationNumber = org.organizationNumber,
            type = "Organization",
            unitType = org.organizationForm,
            authorizedResources = resources,
            isDeleted = false
        )
    }
    return listOf(person) + orgs
}

@Tag("unit")
class AltinnUser {
    private val whitelists: WhitelistProperties = mock()
    private val altinnAdapter: AltinnAdapter = mock()
    private val altinnUserService = AltinnUserService(whitelists, altinnAdapter)

    @BeforeEach
    fun init() {
        whenever(whitelists.adminList).thenReturn(ADMIN_LIST)
        whenever(whitelists.orgNrWhitelist).thenReturn(ORG_NR_LIST)
        whenever(whitelists.orgFormWhitelist).thenReturn(ORG_FORM_LIST)
    }

    @Nested
    internal inner class PersonAuthorities {

        @Test
        fun personNotFoundReturnsEmptyString() {
            runTest {
                val ssn = "12345678901"
                whenever(altinnAdapter.getAuthorizedParties(any())).thenReturn(null)
                assertEquals("", altinnUserService.getAuthorities(ssn))
            }
        }

        @Test
        fun noSSnReturnsNull() {
            runTest {
                val ssn = "12345678901"
                whenever(altinnAdapter.getAuthorizedParties(any())).thenReturn(
                    listOf(
                        AuthorizedParty(name = "First Last", organizationNumber = ORG.organizationNumber, type = "Organization", unitType = ORG.organizationForm, isDeleted = false)
                    )
                )
                assertEquals("", altinnUserService.getAuthorities(ssn))
            }
        }

        @Test
        fun personIsSysAdmin() {
            runTest{
                val ssn = "23076102252"
                whenever(altinnAdapter.getAuthorizedParties(any())).thenReturn(
                    authorizedPartiesForPerson(ssn, organizationsWithResources = emptyList())
                )
                assertEquals(SYS_ADMIN, altinnUserService.getAuthorities(ssn))
            }
        }

        @Test
        fun personIsOrgAdmin() {
            runTest {
                val ssn = "12345678901"
                whenever(altinnAdapter.getAuthorizedParties(any())).thenReturn(
                    authorizedPartiesForPerson(
                        ssn,
                        organizationsWithResources = listOf(ORG to listOf("datanorge-virksomhetsadministrator"))
                    )
                )
                assertEquals(orgAdmin(ORG.organizationNumber!!), altinnUserService.getAuthorities(ssn))
            }
        }

        @Test
        fun personIsOrgWrite() {
            runTest {
                val ssn = "12345678901"
                whenever(altinnAdapter.getAuthorizedParties(any())).thenReturn(
                    authorizedPartiesForPerson(
                        ssn,
                        organizationsWithResources = listOf(ORG to listOf("datanorge-skrivetilgang"))
                    )
                )
                assertEquals(orgWrite(ORG.organizationNumber as String), altinnUserService.getAuthorities(ssn))
            }
        }

        @Test
        fun personHasSeveralRoles() {
            runTest {
                val ssn1 = "23076102252"
                val ssn2 = "12345678901"
                val orgNotInOrgNrWhitelist = AltinnOrganization(
                    name = "Not in orgnr list", organizationNumber = "987654321", organizationForm = "STAT", type = AltinnReporteeType.Organization
                )
                val orgNotInOrgFormWhitelist = AltinnOrganization(
                    name = "Not in org form list", organizationNumber = "123456789", organizationForm = "INVALID", type = AltinnReporteeType.Organization
                )

                whenever(altinnAdapter.getAuthorizedParties(eq(ssn1))).thenReturn(
                    authorizedPartiesForPerson(
                        ssn1,
                        "First1 Last1",
                        listOf(orgNotInOrgNrWhitelist to listOf("datanorge-lesetilgang"))
                    )
                )
                whenever(altinnAdapter.getAuthorizedParties(eq(ssn2))).thenReturn(
                    authorizedPartiesForPerson(
                        ssn2,
                        "First2 Last2",
                        listOf(orgNotInOrgFormWhitelist to listOf("datanorge-skrivetilgang"))
                    )
                )

                val auth1 = altinnUserService.getAuthorities(ssn1)
                assertTrue { auth1.contains(SYS_ADMIN) }
                assertTrue { auth1.contains(orgRead(orgNotInOrgNrWhitelist.organizationNumber as String)) }

                val auth2 = altinnUserService.getAuthorities(ssn2)
                assertTrue { auth2.contains(orgWrite(orgNotInOrgFormWhitelist.organizationNumber as String)) }
            }
        }

    }
    @Nested
    internal inner class OrganizationWhitelistForAuthorities {

        @Test
        fun personWithNoOrganizationsYieldsNoOrgAuthorities() {
            runTest {
                val ssn = "12345678901"
                whenever(altinnAdapter.getAuthorizedParties(any())).thenReturn(
                    authorizedPartiesForPerson(ssn, organizationsWithResources = emptyList())
                )
                assertEquals("", altinnUserService.getAuthorities(ssn))
            }
        }

        @Test
        fun onlyWhitelistedOrganizationsGetRoleTokens() {
            runTest {
                val ssn = "12345678901"
                val org = AltinnOrganization(
                    name = "Org", organizationNumber = "123456789", organizationForm = "STAT", type = AltinnReporteeType.Organization
                )
                val subOrg0 = AltinnOrganization(
                    name = "Whitelisted suborg", organizationNumber = "920210023", organizationForm = "BEDR", type = AltinnReporteeType.Organization
                )
                val subOrg1 = AltinnOrganization(
                    name = "Non whitelisted suborg", organizationNumber = "987654321", organizationForm = "BEDR", type = AltinnReporteeType.Organization
                )
                whenever(altinnAdapter.getAuthorizedParties(any())).thenReturn(
                    authorizedPartiesForPerson(
                        ssn,
                        organizationsWithResources = listOf(
                            org to listOf("datanorge-lesetilgang"),
                            subOrg0 to listOf("datanorge-lesetilgang"),
                            subOrg1 to listOf("datanorge-lesetilgang")
                        )
                    )
                )
                val auth = altinnUserService.getAuthorities(ssn)
                assertTrue { auth.contains(orgRead("123456789")) }
                assertTrue { auth.contains(orgRead("920210023")) }
                assertTrue { !auth.contains(orgRead("987654321")) }
            }
        }

        @Test
        fun whitelistedChildOrgGetsRolesEvenIfParentOrgNotWhitelisted() {
            runTest {
                val ssn = "12345678901"
                val org = AltinnOrganization(
                    name = "Org", organizationNumber = "987654321", organizationForm = "INVALID", type = AltinnReporteeType.Organization
                )
                val subOrg = AltinnOrganization(
                    name = "Whitelisted suborg", organizationNumber = "920210023", organizationForm = "BEDR", type = AltinnReporteeType.Organization
                )
                whenever(altinnAdapter.getAuthorizedParties(any())).thenReturn(
                    authorizedPartiesForPerson(
                        ssn,
                        organizationsWithResources = listOf(
                            org to listOf("datanorge-lesetilgang"),
                            subOrg to listOf("datanorge-lesetilgang")
                        )
                    )
                )
                val auth = altinnUserService.getAuthorities(ssn)
                assertEquals(orgRead("920210023"), auth)
            }
        }
    }

}
