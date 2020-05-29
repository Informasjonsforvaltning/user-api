package no.fdk.userapi.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.fdk.userapi.adapter.TermsAdapter
import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.utils.ORG
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("unit")
class Terms {
    private val termsAdapter: TermsAdapter = mock()
    private val altinnUserService: AltinnUserService = mock()
    private val termsService = TermsService(termsAdapter, altinnUserService)

    @Nested
    internal inner class AltinnTerms {

        @Test
        fun orgHasNotAccepted() {
            val person = AltinnPerson(socialSecurityNumber = "23076102252", name = "First Last", organizations = listOf(ORG))

            whenever(altinnUserService.getUser(person.socialSecurityNumber!!))
                .thenReturn(person)
            whenever(termsAdapter.orgAcceptedTermsVersion(ORG.organizationNumber!!))
                .thenReturn("0.0.0")

            val response = termsService.getOrgTermsAltinn(person.socialSecurityNumber!!)

            assertEquals("${ORG.organizationNumber}:0.0.0", response)
        }

        @Test
        fun orgHasAccepted() {
            val person = AltinnPerson(socialSecurityNumber = "23076102252", name = "First Last", organizations = listOf(ORG))

            whenever(altinnUserService.getUser(person.socialSecurityNumber!!))
                .thenReturn(person)
            whenever(termsAdapter.orgAcceptedTermsVersion(ORG.organizationNumber!!))
                .thenReturn("1.2.3")

            val response = termsService.getOrgTermsAltinn(person.socialSecurityNumber!!)

            assertEquals("${ORG.organizationNumber}:1.2.3", response)
        }

        @Test
        fun severalOrgs() {
            val orgNotAccepted = AltinnOrganization("Org Not Accepted", "ORGL", "123456789")
            val orgAcceptedOld = AltinnOrganization("Org Accepted Old", "ORGL", "987654321")

            val person = AltinnPerson(
                socialSecurityNumber = "23076102252",
                name = "First Last",
                organizations = listOf(ORG, orgNotAccepted, orgAcceptedOld))

            whenever(altinnUserService.getUser(person.socialSecurityNumber!!)).thenReturn(person)

            whenever(termsAdapter.orgAcceptedTermsVersion(ORG.organizationNumber!!)).thenReturn("1.2.3")
            whenever(termsAdapter.orgAcceptedTermsVersion(orgNotAccepted.organizationNumber!!)).thenReturn("0.0.0")
            whenever(termsAdapter.orgAcceptedTermsVersion(orgAcceptedOld.organizationNumber!!)).thenReturn("1.0.0")

            val response = termsService.getOrgTermsAltinn(person.socialSecurityNumber!!)

            val expected = "${ORG.organizationNumber}:1.2.3,${orgNotAccepted.organizationNumber}:0.0.0,${orgAcceptedOld.organizationNumber}:1.0.0"

            assertEquals(expected, response)
        }

    }

    @Nested
    internal inner class DifiTerms {

        @Test
        fun orgHasNotAccepted() {
            val orgNr = "123456789"
            whenever(termsAdapter.orgAcceptedTermsVersion(orgNr)).thenReturn("0.0.0")

            assertEquals("$orgNr:0.0.0", termsService.getOrgTermsDifi(listOf(orgNr)))
        }

        @Test
        fun orgHasAccepted() {
            val orgNr = "123456789"
            whenever(termsAdapter.orgAcceptedTermsVersion(orgNr)).thenReturn("1.2.3")

            assertEquals("$orgNr:1.2.3", termsService.getOrgTermsDifi(listOf(orgNr)))
        }

        @Test
        fun severalOrgs() {
            val org0 = "123456789"
            val org1 = "987654321"
            val org2 = "112233445"
            whenever(termsAdapter.orgAcceptedTermsVersion(org0)).thenReturn("1.2.3")
            whenever(termsAdapter.orgAcceptedTermsVersion(org1)).thenReturn("0.0.0")
            whenever(termsAdapter.orgAcceptedTermsVersion(org2)).thenReturn("1.0.0")

            val response = termsService.getOrgTermsDifi(listOf(org0, org1, org2))

            assertEquals("$org0:1.2.3,$org1:0.0.0,$org2:1.0.0", response)
        }

    }

}