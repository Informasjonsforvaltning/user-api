package no.fdk.userapi.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.fdk.userapi.adapter.AltinnAdapter
import no.fdk.userapi.configuration.WhitelistProperties
import no.fdk.userapi.model.*
import no.fdk.userapi.utils.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
            val ssn = "12345678901"
            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(null)
            assertEquals("", altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun noSSnReturnsNull() {
            val ssn = "12345678901"
            val personNoSSN = AltinnPerson("First Last", null, listOf(ORG))

            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(personNoSSN)

            assertEquals("", altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personIsSysAdmin() {
            val ssn = "23076102252"
            val person = AltinnPerson("First Last", ssn, emptyList())

            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(person)

            assertEquals(SYS_ADMIN, altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personIsOrgAdmin() {
            val ssn = "12345678901"
            val person = AltinnPerson("First Last", ssn, listOf(ORG))
            val rights = AltinnRightsResponse(
                AltinnSubject(
                    name = person.name, socialSecurityNumber = ssn),
                    reportee = AltinnReportee(name = ORG.name, organizationNumber = ORG.organizationNumber),
                    rights = listOf(AltinnRights(serviceCode = "4814")
                )
            )

            whenever(altinnAdapter.getPerson(any(), any())).thenReturn(person)
            whenever(altinnAdapter.getRights(ssn, ORG.organizationNumber!!)).thenReturn(null)

            assertEquals(orgAdmin(ORG.organizationNumber!!), altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personHasSeveralRoles() {
            val ssn = "23076102252"
            val orgNotInOrgNrWhitelist = AltinnOrganization(
                name = "Not in orgnr list", organizationNumber = "987654321", organizationForm = "STAT"
            )
            val orgNotInOrgFormWhitelist = AltinnOrganization(
                name = "Not in org form list", organizationNumber = "123456789", organizationForm = "INVALID"
            )

            whenever(altinnAdapter.getPerson(ssn, "4814")).thenReturn(AltinnPerson("First Last", ssn, listOf(ORG)))
            whenever(altinnAdapter.getPerson(ssn, "5755")).thenReturn(AltinnPerson("First Last", ssn, listOf(orgNotInOrgNrWhitelist)))
            whenever(altinnAdapter.getPerson(ssn, "5756")).thenReturn(AltinnPerson("First Last", ssn, listOf(orgNotInOrgFormWhitelist)))

            whenever(altinnAdapter.getRights(ssn, ORG.organizationNumber!!)).thenReturn(null)
            whenever(altinnAdapter.getRights(ssn, orgNotInOrgNrWhitelist.organizationNumber!!)).thenReturn(
                AltinnRightsResponse(
                    AltinnSubject(name = "First Last", socialSecurityNumber = ssn),
                        reportee = AltinnReportee(name = orgNotInOrgNrWhitelist.name, organizationNumber = orgNotInOrgNrWhitelist.organizationNumber),
                        rights = listOf(AltinnRights(serviceCode = "5756"))
                )
            )
            whenever(altinnAdapter.getRights(ssn, orgNotInOrgFormWhitelist.organizationNumber!!)).thenReturn(
                AltinnRightsResponse(
                    AltinnSubject(name = "First Last", socialSecurityNumber = ssn),
                        reportee = AltinnReportee(name = orgNotInOrgFormWhitelist.name, organizationNumber = orgNotInOrgFormWhitelist.organizationNumber),
                        rights = listOf(AltinnRights(serviceCode = "5755"))
                )
            )

            assertEquals("$SYS_ADMIN,${orgAdmin(ORG.organizationNumber!!)},${orgRead(orgNotInOrgNrWhitelist.organizationNumber!!)},${orgAdmin(orgNotInOrgFormWhitelist.organizationNumber!!)}", altinnUserService.getAuthorities(ssn))
        }

    }

}
