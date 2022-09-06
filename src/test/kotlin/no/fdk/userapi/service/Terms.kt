package no.fdk.userapi.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.fdk.userapi.adapter.TermsAdapter
import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.AltinnReporteeType
import no.fdk.userapi.utils.ORG
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("unit")
class Terms {
    private val termsAdapter: TermsAdapter = mock()
    private val altinnAuthActivity: AltinnAuthActivity = mock()
    private val termsService = TermsService(termsAdapter, altinnAuthActivity)

    @Nested
    internal inner class AltinnTerms {

        @Test
        fun orgHasNotAccepted() {
            val person = AltinnPerson(socialSecurityNumber = "23076102252", name = "First Last", organizations = listOf(ORG))

            whenever(altinnAuthActivity.getOrganizationsForTerms(person.socialSecurityNumber!!))
                .thenReturn(person.organizations)
            whenever(termsAdapter.orgAcceptedTermsVersion(ORG.organizationNumber!!))
                .thenReturn("0.0.0")

            val response = termsService.getOrgTermsAltinn(person.socialSecurityNumber as String)

            assertEquals("${ORG.organizationNumber}:0.0.0", response)
        }

        @Test
        fun orgHasAccepted() {
            val person = AltinnPerson(socialSecurityNumber = "23076102252", name = "First Last", organizations = listOf(ORG))

            whenever(altinnAuthActivity.getOrganizationsForTerms(person.socialSecurityNumber!!))
                .thenReturn(person.organizations)
            whenever(termsAdapter.orgAcceptedTermsVersion(ORG.organizationNumber!!))
                .thenReturn("1.2.3")

            val response = termsService.getOrgTermsAltinn(person.socialSecurityNumber as String)

            assertEquals("${ORG.organizationNumber}:1.2.3", response)
        }

        @Test
        fun severalOrgs() {
            val orgNotAccepted = AltinnOrganization("Org Not Accepted", "ORGL", "123456789", type = AltinnReporteeType.Enterprise)
            val orgAcceptedOld = AltinnOrganization("Org Accepted Old", "ORGL", "987654321", type = AltinnReporteeType.Enterprise)

            val person = AltinnPerson(
                socialSecurityNumber = "23076102252",
                name = "First Last",
                organizations = listOf(ORG, orgNotAccepted, orgAcceptedOld))

            whenever(altinnAuthActivity.getOrganizationsForTerms(person.socialSecurityNumber!!))
                .thenReturn(person.organizations)

            whenever(termsAdapter.orgAcceptedTermsVersion(ORG.organizationNumber!!)).thenReturn("1.2.3")
            whenever(termsAdapter.orgAcceptedTermsVersion(orgNotAccepted.organizationNumber!!)).thenReturn("0.0.0")
            whenever(termsAdapter.orgAcceptedTermsVersion(orgAcceptedOld.organizationNumber!!)).thenReturn("1.0.0")

            val response = termsService.getOrgTermsAltinn(person.socialSecurityNumber as String)

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

    @Nested
    internal inner class OsloKommuneTerms {

        @Test
        fun orgHasNotAccepted() {
            val org = Pair("Oslo Havn KF", "987592567")
            whenever(termsAdapter.orgAcceptedTermsVersion(org.second)).thenReturn("0.0.0")

            assertEquals("${org.second}:0.0.0", termsService.getOrgTermsOk(listOf(org.first)))
        }

        @Test
        fun orgHasAccepted() {
            val org = Pair("Drift", "971183675")
            whenever(termsAdapter.orgAcceptedTermsVersion(org.second)).thenReturn("1.2.3")

            assertEquals("${org.second}:1.2.3", termsService.getOrgTermsOk(listOf(org.first)))
        }

        @Test
        fun severalOrgs() {
            val org0 = Pair("Drift", "971183675")
            val org1 = Pair("Oslo Havn KF", "987592567")
            val org2 = Pair("Renovasjons- og gjenvinningsetaten", "923954791")
            whenever(termsAdapter.orgAcceptedTermsVersion(org0.second)).thenReturn("1.2.3")
            whenever(termsAdapter.orgAcceptedTermsVersion(org1.second)).thenReturn("0.0.0")
            whenever(termsAdapter.orgAcceptedTermsVersion(org2.second)).thenReturn("1.0.0")

            val response = termsService.getOrgTermsOk(listOf(org0.first, org1.first, org2.first))

            assertEquals("${org0.second}:1.2.3,${org1.second}:0.0.0,${org2.second}:1.0.0", response)
        }

    }

}
