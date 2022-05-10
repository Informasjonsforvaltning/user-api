package no.fdk.userapi.mapper

import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.AltinnReportee
import no.fdk.userapi.model.AltinnRights
import no.fdk.userapi.model.AltinnRightsResponse
import no.fdk.userapi.model.AltinnSubject
import no.fdk.userapi.model.RoleFDK
import no.fdk.userapi.model.UserFDK
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class Altinn {

    @Nested
    internal inner class PersonToUser {

        @Test
        fun noSSNReturnsNull() {
            val person = AltinnPerson(
                name = "First Last",
                socialSecurityNumber = null,
                organizations = emptyList()
            )

            assertNull(person.toUserFDK())
        }

        @Test
        fun lastNameIsSeparatedFromName() {
            val person = AltinnPerson(
                name = "First Middle Last",
                socialSecurityNumber = "23076102252",
                organizations = emptyList()
            )

            val expected = UserFDK(
                id = "23076102252",
                firstName = "First Middle",
                lastName = "Last"
            )

            assertEquals(expected, person.toUserFDK())
        }

    }

    @Nested
    internal inner class AltinnRightsToFDK {

        @Test
        fun filterSimilarRoles() {
            val rights = AltinnRightsResponse(
                subject = AltinnSubject(name="Test Person", type="Person", organizationNumber=null, organizationForm=null, status=null, socialSecurityNumber="11115601999"),
                reportee= AltinnReportee(name="TESTLANDET REGNSKAP", organizationNumber="999888777", type="Enterprise", status="Active"),
                rights=listOf(
                    AltinnRights(id="13441077", type="Service", serviceCode="5755", serviceEditionCode="1"),
                    AltinnRights(id="13441078", type="Service", serviceCode="5756", serviceEditionCode="1"),
                    AltinnRights(id="4345871", type="Service", serviceCode="5756", serviceEditionCode="1"),
                    AltinnRights(id="4345873", type="Service", serviceCode="5755", serviceEditionCode="1")))

            val expected = listOf(
                RoleFDK(resourceType = RoleFDK.ResourceType.Organization, resourceId = "999888777", role = RoleFDK.Role.Admin),
                RoleFDK(resourceType = RoleFDK.ResourceType.Organization, resourceId = "999888777", role = RoleFDK.Role.Read))

            assertEquals(expected, rights.toFDKRoles())
        }
    }

}
