package no.fdk.userapi.service

import kotlinx.coroutines.test.runTest
import no.fdk.userapi.adapter.TermsAdapter
import no.fdk.userapi.configuration.BRREGProperties
import no.fdk.userapi.configuration.SkattProperties
import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.AltinnReporteeType
import no.fdk.userapi.utils.ORG
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@Tag("unit")
class Terms {
    private val termsAdapter: TermsAdapter = mock()
    private val altinnUserService: AltinnUserService = mock()
    private val brregProperties: BRREGProperties = mock()
    private val skattProperties: SkattProperties = mock()
    private val termsService = TermsService(termsAdapter, altinnUserService, brregProperties, skattProperties)

    @Nested
    internal inner class AltinnTerms {

        @Test
        fun orgHasNotAccepted() = runTest {
            val person = AltinnPerson(socialSecurityNumber = "23076102252", name = "First Last", organizations = listOf(ORG))

            whenever(altinnUserService.getOrganizationsForTerms(person.socialSecurityNumber!!))
                .thenReturn(person.organizations)
            whenever(termsAdapter.orgAcceptedTermsVersion(ORG.organizationNumber!!))
                .thenReturn("0.0.0")

            val response = termsService.getOrgTermsAltinn(person.socialSecurityNumber)

            assertEquals("", response)
        }

        @Test
        fun orgHasAccepted() = runTest {
            val person = AltinnPerson(socialSecurityNumber = "23076102252", name = "First Last", organizations = listOf(ORG))

            whenever(altinnUserService.getOrganizationsForTerms(person.socialSecurityNumber!!))
                .thenReturn(person.organizations)
            whenever(termsAdapter.orgAcceptedTermsVersion(ORG.organizationNumber!!))
                .thenReturn("1.2.3")

            val response = termsService.getOrgTermsAltinn(person.socialSecurityNumber)

            assertEquals("${ORG.organizationNumber}:1.2.3", response)
        }

        @Test
        fun severalOrgs() = runTest {
            val orgNotAccepted = AltinnOrganization("Org Not Accepted", "ORGL", "123456789", type = AltinnReporteeType.Enterprise)
            val orgAcceptedOld = AltinnOrganization("Org Accepted Old", "ORGL", "987654321", type = AltinnReporteeType.Enterprise)

            val person = AltinnPerson(
                socialSecurityNumber = "23076102252",
                name = "First Last",
                organizations = listOf(ORG, orgNotAccepted, orgAcceptedOld))

            whenever(altinnUserService.getOrganizationsForTerms(person.socialSecurityNumber!!))
                .thenReturn(person.organizations)

            whenever(termsAdapter.orgAcceptedTermsVersion(ORG.organizationNumber!!)).thenReturn("1.2.3")
            whenever(termsAdapter.orgAcceptedTermsVersion(orgNotAccepted.organizationNumber!!)).thenReturn("0.0.0")
            whenever(termsAdapter.orgAcceptedTermsVersion(orgAcceptedOld.organizationNumber!!)).thenReturn("1.0.0")

            val response = termsService.getOrgTermsAltinn(person.socialSecurityNumber)

            val expected = "${ORG.organizationNumber}:1.2.3,${orgAcceptedOld.organizationNumber}:1.0.0"

            assertEquals(expected, response)
        }

    }

    @Nested
    internal inner class DifiTerms {

        @Test
        fun orgHasNotAccepted() {
            val orgNr = "123456789"
            whenever(termsAdapter.orgAcceptedTermsVersion(orgNr)).thenReturn("0.0.0")

            assertEquals("", termsService.getOrgTermsDifi(listOf(orgNr)))
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

            assertEquals("$org0:1.2.3,$org2:1.0.0", response)
        }

    }

    @Nested
    internal inner class BRREGTerms {

        @Test
        fun orgHasNotAccepted() {
            whenever(brregProperties.orgnr).thenReturn("974760673")
            whenever(termsAdapter.orgAcceptedTermsVersion("974760673")).thenReturn("0.0.0")

            assertEquals("", termsService.getOrgTermsBRREG())
        }

        @Test
        fun orgHasAccepted() {
            whenever(brregProperties.orgnr).thenReturn("974760673")
            whenever(termsAdapter.orgAcceptedTermsVersion("974760673")).thenReturn("1.2.3")

            assertEquals("974760673:1.2.3", termsService.getOrgTermsBRREG())
        }

    }

    @Nested
    internal inner class SkattTerms {

        @Test
        fun orgHasNotAccepted() {
            whenever(skattProperties.orgnr).thenReturn("974761076")
            whenever(termsAdapter.orgAcceptedTermsVersion("974761076")).thenReturn("0.0.0")

            assertEquals("", termsService.getOrgTermsSkatt())
        }

        @Test
        fun orgHasAccepted() {
            whenever(skattProperties.orgnr).thenReturn("974761076")
            whenever(termsAdapter.orgAcceptedTermsVersion("974761076")).thenReturn("1.2.3")

            assertEquals("974761076:1.2.3", termsService.getOrgTermsSkatt())
        }

    }

}
