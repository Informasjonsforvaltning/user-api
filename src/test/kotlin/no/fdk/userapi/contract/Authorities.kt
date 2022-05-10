package no.fdk.userapi.contract

import no.fdk.userapi.utils.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    properties = ["spring.profiles.active=contract-test"],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Tag("contract")
class Authorities : WiremockContext() {

    @Nested
    internal inner class AltinnAuthorities {

        @Test
        fun forbiddenWithWrongApiKey() {
            val response = apiGet(
                path = "/authorities/altinn/12345678901",
                headers = mapOf(Pair("X-API-KEY", "wrong-key")))

            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun respondWithEmptyAuthStringWhenNotFoundInAltinn() {
            val response = apiGet(
                path = "/authorities/altinn/12345678901",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("", response["body"])
        }

        @Test
        fun respondWithBothSysAdminAndOrgAdminWhenInAdminList() {
            val response = apiGet(
                path = "/authorities/altinn/10987654321",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            val body: String = response["body"] as String
            assertTrue { body.contains("system:root:admin") }
            assertTrue { body.contains("organization:920210023:admin") }
        }

        @Test
        fun respondWithAllOrgAdminWhenInAssociatedWithSeveralOrganizations() {
            val response = apiGet(
                path = "/authorities/altinn/11223344556",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            val body: String = response["body"] as String
            assertTrue { body.contains("organization:910258028:admin") }
            assertTrue { body.contains("organization:123456789:read") }
        }

    }

    @Nested
    internal inner class DifiAuthorities {

        @Test
        fun forbiddenWithWrongApiKey() {
            val response = apiGet(
                path = "/authorities/difi?roles=editor&orgs=",
                headers = mapOf(Pair("X-API-KEY", "wrong-key")))

            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun sysAdminDoesNotNeedToBeAssociatedWithOrg() {
            val response = apiGet(
                path = "/authorities/difi?roles=editor&orgs=",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("system:root:admin", response["body"])
        }

        @Test
        fun sysAdminNotCountedInOrgAssociations() {
            val response = apiGet(
                path = "/authorities/difi?roles=author,editor,subscriber&orgs=123456789,910258028,920210023",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            val body: String = response["body"] as String
            assertTrue { body.contains("organization:123456789:admin") }
            assertTrue { body.contains("system:root:admin") }
            assertTrue { body.contains("organization:910258028:read") }
        }

        @Test
        fun orgRolesIgnoredWhenMissingOrgAssociations() {
            val response = apiGet(
                path = "/authorities/difi?roles=contributor,author,subscriber&orgs=123456789",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("organization:123456789:write", response["body"])
        }

    }

    @Nested
    internal inner class OsloKommune {

        @Test
        fun forbiddenWithWrongApiKey() {
            val response = apiGet(
                path = "/authorities/oslokommune?roles=$OK_ADMIN&orgnames=Drift",
                headers = mapOf(Pair("X-API-KEY", "wrong-key")))

            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun orgRolesIgnoredWhenMissingOrgAssociations() {
            val response = apiGet(
                path = "/authorities/oslokommune?roles=$OK_ADMIN,$OK_WRITE,$OK_READ&orgnames=Drift",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("organization:971183675:admin", response["body"])
        }

    }

}
