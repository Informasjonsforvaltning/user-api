package no.fdk.userapi.mapper

import no.fdk.userapi.model.AltinnPerson
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

}