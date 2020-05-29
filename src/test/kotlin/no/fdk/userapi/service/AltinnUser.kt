package no.fdk.userapi.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.fdk.userapi.adapter.AltinnAdapter
import no.fdk.userapi.configuration.WhitelistProperties
import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.utils.ADMIN_LIST
import no.fdk.userapi.utils.ORG
import no.fdk.userapi.utils.ORG_FORM_LIST
import no.fdk.userapi.utils.ORG_NR_LIST
import no.fdk.userapi.utils.SYS_ADMIN
import no.fdk.userapi.utils.orgAdmin
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
        fun personNotFoundReturnsNull() {
            val ssn = "23076102252"
            whenever(altinnAdapter.getPerson(ssn)).thenReturn(null)
            assertNull(altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun noSSnReturnsNull() {
            val ssn = "12345678901"
            val personNoSSN = AltinnPerson("First Last", null, listOf(ORG))

            whenever(altinnAdapter.getPerson(ssn)).thenReturn(personNoSSN)

            assertNull(altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personIsSysAdmin() {
            val ssn = "23076102252"
            val person = AltinnPerson("First Last", ssn, emptyList())

            whenever(altinnAdapter.getPerson(ssn)).thenReturn(person)

            assertEquals(SYS_ADMIN, altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personIsOrgAdmin() {
            val ssn = "12345678901"
            val person = AltinnPerson("First Last", ssn, listOf(ORG))

            whenever(altinnAdapter.getPerson(ssn)).thenReturn(person)

            assertEquals(orgAdmin(ORG.organizationNumber!!), altinnUserService.getAuthorities(ssn))
        }

        @Test
        fun personHasSeveralRoles() {
            val ssn = "23076102252"
            val orgNotInOrgNrWhitelist = AltinnOrganization(
                name = "Not in orgnr list", organizationNumber = "987654321", organizationForm = "STAT")
            val orgNotInOrgFormWhitelist = AltinnOrganization(
                name = "Not in org form list", organizationNumber = "123456789", organizationForm = "INVALID")
            val orgOk = AltinnOrganization(
                name = "Org OK", organizationNumber = "910258028", organizationForm = "STAT")
            val person = AltinnPerson(
                "First Last",
                ssn,
                listOf(ORG, orgNotInOrgNrWhitelist, orgNotInOrgFormWhitelist, orgOk))

            whenever(altinnAdapter.getPerson(ssn)).thenReturn(person)

            assertEquals("${orgAdmin(ORG.organizationNumber!!)},${orgAdmin(orgOk.organizationNumber!!)},$SYS_ADMIN", altinnUserService.getAuthorities(ssn))
        }

    }

}