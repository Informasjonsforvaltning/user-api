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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        fun personNotFoundReturnsEmptyString() = runTest {
            val ssn = "12345678901"
            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(null)
            assertEquals("", altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun noSSnReturnsNull() = runTest {
            val ssn = "12345678901"
            val personNoSSN = AltinnPerson("First Last", null, listOf(ORG))

            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(personNoSSN)

            assertEquals("", altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personIsSysAdmin() = runTest{
            val ssn = "23076102252"
            val person = AltinnPerson("First Last", ssn, emptyList())

            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(person)

            assertEquals(SYS_ADMIN, altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personIsOrgAdmin() = runTest {
            val ssn = "12345678901"
            val person = AltinnPerson("First Last", ssn, listOf(ORG))
            val rights = AltinnRightsResponse(
                AltinnSubject(
                    name = person.name, socialSecurityNumber = ssn),
                    reportee = AltinnReportee(name = ORG.name, organizationNumber = ORG.organizationNumber),
                    rights = listOf(AltinnRights(serviceCode = "5977")
                )
            )

            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(person)
            whenever(altinnAdapter.getRights(ssn, ORG.organizationNumber!!)).thenReturn(rights)

            assertEquals(orgAdmin(ORG.organizationNumber), altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personIsOrgWrite() = runTest {
            val ssn = "12345678901"
            val person = AltinnPerson("First Last", ssn, listOf(ORG))
            val rights = AltinnRightsResponse(
                AltinnSubject(
                    name = person.name, socialSecurityNumber = ssn),
                reportee = AltinnReportee(name = ORG.name, organizationNumber = ORG.organizationNumber),
                rights = listOf(AltinnRights(serviceCode = "5755")
                )
            )

            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(person)
            whenever(altinnAdapter.getRights(ssn, ORG.organizationNumber!!)).thenReturn(rights)

            assertEquals(orgWrite(ORG.organizationNumber as String), altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personHasSeveralRoles() = runTest {
            val ssn1 = "23076102252"
            val ssn2 = "12345678901"
            val orgNotInOrgNrWhitelist = AltinnOrganization(
                name = "Not in orgnr list", organizationNumber = "987654321", organizationForm = "STAT", type = AltinnReporteeType.Enterprise
            )
            val orgNotInOrgFormWhitelist = AltinnOrganization(
                name = "Not in org form list", organizationNumber = "123456789", organizationForm = "INVALID", type = AltinnReporteeType.Enterprise
            )

            whenever(altinnAdapter.getPerson(ssn1, "5755")).thenReturn(AltinnPerson("First1 Last1", ssn1, listOf(orgNotInOrgNrWhitelist)))
            whenever(altinnAdapter.getPerson(ssn2, "5756")).thenReturn(AltinnPerson("First2 Last2", ssn2, listOf(orgNotInOrgFormWhitelist)))

            whenever(altinnAdapter.getRights(ssn1, orgNotInOrgNrWhitelist.organizationNumber!!)).thenReturn(
                AltinnRightsResponse(
                    AltinnSubject(name = "First1 Last1", socialSecurityNumber = ssn1),
                        reportee = AltinnReportee(name = orgNotInOrgNrWhitelist.name, organizationNumber = orgNotInOrgNrWhitelist.organizationNumber),
                        rights = listOf(AltinnRights(serviceCode = "5756"))
                )
            )
            whenever(altinnAdapter.getRights(ssn2, orgNotInOrgFormWhitelist.organizationNumber!!)).thenReturn(
                AltinnRightsResponse(
                    AltinnSubject(name = "First2 Last2", socialSecurityNumber = ssn2),
                        reportee = AltinnReportee(name = orgNotInOrgFormWhitelist.name, organizationNumber = orgNotInOrgFormWhitelist.organizationNumber),
                        rights = listOf(AltinnRights(serviceCode = "5755"))
                )
            )

            val auth1 = altinnUserService.getAuthorities(ssn1)
            assertTrue { auth1.contains(SYS_ADMIN) }
            assertTrue { auth1.contains(orgRead(orgNotInOrgNrWhitelist.organizationNumber as String)) }

            val auth2 = altinnUserService.getAuthorities(ssn2)
            assertTrue { auth2.contains(orgWrite(orgNotInOrgFormWhitelist.organizationNumber as String)) }
        }

    }
    @Nested
    internal inner class PersonOrganizations {

        @Test
        fun personNotFoundReturnsEmptyList() = runTest {
            val ssn = "12345678901"
            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(null)
            assertEquals(emptyList(), altinnUserService.organizationsForService(ssn, "5755"))
        }

        @Test
        fun handlesEmptyOrgList() = runTest {
            val ssn = "12345678901"
            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(AltinnPerson("First1 Last1", ssn, emptyList()))
            assertEquals(emptyList(), altinnUserService.organizationsForService(ssn, "5755"))
        }

        @Test
        fun whitelistedSubOrgIsIncluded() = runTest {
            val ssn = "12345678901"
            val org = AltinnOrganization(
                name = "Org", organizationNumber = "123456789", organizationForm = "STAT", type = AltinnReporteeType.Enterprise
            )
            val subOrg0 = AltinnOrganization(
                name = "Whitelisted suborg", organizationNumber = "920210023", organizationForm = "BEDR", type = AltinnReporteeType.Business
            )
            val subOrg1 = AltinnOrganization(
                name = "Non whitelisted suborg", organizationNumber = "987654321", organizationForm = "BEDR", type = AltinnReporteeType.Business
            )
            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(AltinnPerson("First1 Last1", ssn, listOf(org, subOrg0, subOrg1)))
            assertEquals(listOf(org, subOrg0), altinnUserService.organizationsForService(ssn, "5755"))
        }

        @Test
        fun subOrgOfNonWhitelistedIsStillIncluded() = runTest {
            val ssn = "12345678901"
            val org = AltinnOrganization(
                name = "Org", organizationNumber = "987654321", organizationForm = "INVALID", type = AltinnReporteeType.Enterprise
            )
            val subOrg = AltinnOrganization(
                name = "Whitelisted suborg", organizationNumber = "920210023", organizationForm = "BEDR", type = AltinnReporteeType.Business
            )
            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(AltinnPerson("First1 Last1", ssn, listOf(org, subOrg)))
            assertEquals(listOf(subOrg), altinnUserService.organizationsForService(ssn, "5755"))
        }

    }

}
