package no.fdk.userapi.mapper

import no.fdk.userapi.utils.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("unit")
class OsloKommune {

    @Test
    fun noInputRolesReturnsEmptyString() {
        val emptyInput = mapAuthoritiesFromOK(emptyList(), emptyList())
        val withOrg = mapAuthoritiesFromOK(emptyList(), listOf("Drift"))
        val randomRole = mapAuthoritiesFromOK(listOf("random"), listOf("Drift"))

        assertEquals("", emptyInput)
        assertEquals("", withOrg)
        assertEquals("", randomRole)
    }

    @Test
    fun orgRoles() {
        val orgName = "Drift"

        val admin = mapAuthoritiesFromOK(listOf(OK_ADMIN), listOf(orgName))
        val write = mapAuthoritiesFromOK(listOf(OK_WRITE), listOf(orgName))
        val read = mapAuthoritiesFromOK(listOf(OK_READ), listOf(orgName))

        val orgNr = "971183675"

        assertEquals(orgAdmin(orgNr), admin)
        assertEquals(orgWrite(orgNr), write)
        assertEquals(orgRead(orgNr), read)
    }

    @Test
    fun rolesDemandsOrgName() {
        val orgName = "Drift"

        val emptyOrgs = mapAuthoritiesFromOK(listOf(OK_READ), emptyList())
        val notEnoughOrgs = mapAuthoritiesFromOK(listOf(OK_ADMIN, OK_WRITE), listOf(orgName))

        assertEquals("", emptyOrgs)
        assertEquals(orgAdmin("971183675"), notEnoughOrgs)
    }

    @Test
    fun severalRolesIsCommaSeparated() {
        val org0 = Pair("Drift", "971183675")
        val org1 = Pair("Oslo Havn KF", "987592567")
        val org2 = Pair("Renovasjons- og gjenvinningsetaten", "923954791")

        val commaSeparated = mapAuthoritiesFromOK(listOf(OK_ADMIN, OK_WRITE, OK_READ), listOf(org0.first, org1.first, org2.first))

        assertEquals("${orgAdmin(org0.second)},${orgWrite(org1.second)},${orgRead(org2.second)}", commaSeparated)
    }

}
