package no.fdk.userapi.mapper

import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.AuthorizedParty
import no.fdk.userapi.model.RoleFDK
import no.fdk.userapi.model.UserFDK
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class Altinn {

    @Nested
    internal inner class PersonToUser {

        @Test
        fun noSSNReturnsNull() {
            val person = AltinnPerson(
                name = "First Last",
                socialSecurityNumber = null,
                organizations = emptyList(),
            )

            assertNull(person.toUserFDK())
        }

        @Test
        fun lastNameIsSeparatedFromName() {
            val person = AltinnPerson(
                name = "First Middle Last",
                socialSecurityNumber = "23076102252",
                organizations = emptyList(),
            )

            val expected = UserFDK(
                id = "23076102252",
                firstName = "First Middle",
                lastName = "Last",
            )

            assertEquals(expected, person.toUserFDK())
        }
    }

    @Nested
    internal inner class AuthorizedPartiesToFDKRoles {

        @Test
        fun mapsResourceIdsToRoles() {
            val parties = listOf(
                AuthorizedParty(
                    organizationNumber = "999888777",
                    type = "Organization",
                    authorizedResources = listOf("datanorge-lesetilgang", "datanorge-skrivetilgang"),
                ),
            )
            val result = parties.toFDKRoles("999888777")
            val expected = listOf(
                RoleFDK(RoleFDK.ResourceType.Organization, "999888777", RoleFDK.Role.Read),
                RoleFDK(RoleFDK.ResourceType.Organization, "999888777", RoleFDK.Role.Write),
            )
            assertEquals(expected.size, result.size)
            assertTrue(result.containsAll(expected))
        }

        @Test
        fun mapsAccessPackageToAdminRole() {
            val parties = listOf(
                AuthorizedParty(
                    organizationNumber = "999888777",
                    type = "Organization",
                    authorizedAccessPackages = listOf("informasjon-og-kommunikasjon"),
                ),
            )
            val expected = listOf(RoleFDK(RoleFDK.ResourceType.Organization, "999888777", RoleFDK.Role.Admin))
            assertEquals(expected, parties.toFDKRoles("999888777"))
        }

        @Test
        fun ignoresUnknownAccessPackages() {
            val parties = listOf(
                AuthorizedParty(
                    organizationNumber = "999888777",
                    type = "Organization",
                    authorizedAccessPackages = listOf("skatt-og-avgift", "regnskap-og-okonomirapportering"),
                ),
            )
            assertTrue(parties.toFDKRoles("999888777").isEmpty())
        }

        @Test
        fun combinesRolesFromAccessPackagesAndResources() {
            val parties = listOf(
                AuthorizedParty(
                    organizationNumber = "999888777",
                    type = "Organization",
                    authorizedResources = listOf("datanorge-lesetilgang"),
                    authorizedAccessPackages = listOf("skatt-og-avgift", "informasjon-og-kommunikasjon"),
                ),
            )
            val result = parties.toFDKRoles("999888777")
            val expected = listOf(
                RoleFDK(RoleFDK.ResourceType.Organization, "999888777", RoleFDK.Role.Admin),
                RoleFDK(RoleFDK.ResourceType.Organization, "999888777", RoleFDK.Role.Read),
            )
            assertEquals(expected.size, result.size)
            assertTrue(result.containsAll(expected))
        }

        @Test
        fun adminFromBothAccessPackageAndResourceIsReturnedOnce() {
            val parties = listOf(
                AuthorizedParty(
                    organizationNumber = "999888777",
                    type = "Organization",
                    authorizedResources = listOf("datanorge-virksomhetsadministrator"),
                    authorizedAccessPackages = listOf("informasjon-og-kommunikasjon"),
                ),
            )
            val expected = listOf(RoleFDK(RoleFDK.ResourceType.Organization, "999888777", RoleFDK.Role.Admin))
            assertEquals(expected, parties.toFDKRoles("999888777"))
        }

        @Test
        fun accessPackagesOnOtherOrgAreNotApplied() {
            val parties = listOf(
                AuthorizedParty(
                    organizationNumber = "111222333",
                    type = "Organization",
                    authorizedAccessPackages = listOf("informasjon-og-kommunikasjon"),
                ),
                AuthorizedParty(
                    organizationNumber = "999888777",
                    type = "Organization",
                    authorizedResources = listOf("datanorge-lesetilgang"),
                ),
            )
            val expected = listOf(RoleFDK(RoleFDK.ResourceType.Organization, "999888777", RoleFDK.Role.Read))
            assertEquals(expected, parties.toFDKRoles("999888777"))
        }

        @Test
        fun unknownOrgReturnsEmpty() {
            val parties = listOf(AuthorizedParty(organizationNumber = "111", type = "Organization"))
            assertTrue(parties.toFDKRoles("999").isEmpty())
        }
    }

    @Nested
    internal inner class AccessPackageToRole {

        @Test
        fun informasjonOgKommunikasjonIsAdmin() {
            assertEquals(RoleFDK.Role.Admin, accessPackageToRole("informasjon-og-kommunikasjon"))
        }

        @Test
        fun unknownAccessPackageIsNull() {
            assertNull(accessPackageToRole("skatt-og-avgift"))
            assertNull(accessPackageToRole(""))
        }
    }
}
