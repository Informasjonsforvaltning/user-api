package no.fdk.userapi.mapper

import no.fdk.userapi.utils.AUTHOR_ROLE
import no.fdk.userapi.utils.CONTRIBUTOR_ROLE
import no.fdk.userapi.utils.EDITOR_ROLE
import no.fdk.userapi.utils.SUBSCRIBER_ROLE
import no.fdk.userapi.utils.SYS_ADMIN
import no.fdk.userapi.utils.orgAdmin
import no.fdk.userapi.utils.orgRead
import no.fdk.userapi.utils.orgWrite
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("unit")
class DifiAuth {

    @Test
    fun noInputRolesReturnsEmptyString() {
        val nullInput = mapAuthoritiesFromDifiRole(null, null)
        val emptyInput = mapAuthoritiesFromDifiRole(emptyList(), emptyList())
        val withOrg = mapAuthoritiesFromDifiRole(emptyList(), listOf("123456789"))
        val randomRole = mapAuthoritiesFromDifiRole(listOf("random"), listOf("123456789"))

        assertEquals("", nullInput)
        assertEquals("", emptyInput)
        assertEquals("", withOrg)
        assertEquals("", randomRole)
    }

    @Test
    fun editorToSysAdmin() {
        val noOrg = mapAuthoritiesFromDifiRole(listOf(EDITOR_ROLE), null)
        val withOrg = mapAuthoritiesFromDifiRole(listOf(EDITOR_ROLE), listOf("123456789"))

        assertEquals(SYS_ADMIN, noOrg)
        assertEquals(SYS_ADMIN, withOrg)
    }

    @Test
    fun orgRoles() {
        val orgNr = "123456789"

        val admin = mapAuthoritiesFromDifiRole(listOf(AUTHOR_ROLE), listOf(orgNr))
        val write = mapAuthoritiesFromDifiRole(listOf(CONTRIBUTOR_ROLE), listOf(orgNr))
        val read = mapAuthoritiesFromDifiRole(listOf(SUBSCRIBER_ROLE), listOf(orgNr))

        assertEquals(orgAdmin(orgNr), admin)
        assertEquals(orgWrite(orgNr), write)
        assertEquals(orgRead(orgNr), read)
    }

    @Test
    fun orgRolesDemandsOrgNr() {
        val orgNr = "123456789"

        val nullOrgs = mapAuthoritiesFromDifiRole(listOf(AUTHOR_ROLE), null)
        val emptyOrgs = mapAuthoritiesFromDifiRole(listOf(CONTRIBUTOR_ROLE), emptyList())
        val notEnoughOrgs = mapAuthoritiesFromDifiRole(listOf(AUTHOR_ROLE, SUBSCRIBER_ROLE), listOf(orgNr))

        assertEquals("", nullOrgs)
        assertEquals("", emptyOrgs)
        assertEquals(orgAdmin(orgNr), notEnoughOrgs)
    }

    @Test
    fun severalRolesIsCommaSeparated() {
        val org0 = "123456789"
        val org1 = "987654321"
        val org2 = "112233445"

        val commaSeparated = mapAuthoritiesFromDifiRole(listOf(AUTHOR_ROLE, CONTRIBUTOR_ROLE, SUBSCRIBER_ROLE), listOf(org0, org1, org2))

        assertEquals("${orgAdmin(org0)},${orgWrite(org1)},${orgRead(org2)}", commaSeparated)
    }

    @Test
    fun sysAdminRoleNotCountedInorgToRoleConnection() {
        val org0 = "123456789"
        val org1 = "987654321"
        val org2 = "112233445"

        val sysAdminFirst = mapAuthoritiesFromDifiRole(listOf(EDITOR_ROLE, CONTRIBUTOR_ROLE, SUBSCRIBER_ROLE), listOf(org0, org1, org2))
        val sysAdminMiddle = mapAuthoritiesFromDifiRole(listOf(AUTHOR_ROLE, EDITOR_ROLE, SUBSCRIBER_ROLE), listOf(org0, org1, org2))
        val sysAdminAfterDefectiveOrgRoles = mapAuthoritiesFromDifiRole(listOf(SUBSCRIBER_ROLE, CONTRIBUTOR_ROLE, EDITOR_ROLE), listOf(org2))

        assertEquals("$SYS_ADMIN,${orgWrite(org0)},${orgRead(org1)}", sysAdminFirst)
        assertEquals("${orgAdmin(org0)},$SYS_ADMIN,${orgRead(org1)}", sysAdminMiddle)
        assertEquals("${orgRead(org2)},$SYS_ADMIN", sysAdminAfterDefectiveOrgRoles)
    }

}