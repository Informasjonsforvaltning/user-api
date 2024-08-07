package no.fdk.userapi.contract

import no.fdk.userapi.utils.SSO_KEY
import no.fdk.userapi.utils.WiremockContext
import no.fdk.userapi.utils.apiGet
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
        fun respondWithBothSysAdminAndOrgRoleWhenInAdminList() {
            val response = apiGet(
                path = "/authorities/altinn/10987654321",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            val body: String = response["body"] as String
            assertTrue { body.contains("system:root:admin") }
            assertTrue { body.contains("organization:920210023:write") }
        }

        @Test
        fun respondWithAllOrgRolesWhenInAssociatedWithSeveralOrganizations() {
            val response = apiGet(
                path = "/authorities/altinn/11223344556",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            val body: String = response["body"] as String
            assertTrue { body.contains("organization:910258028:write") }
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
    internal inner class BRREG {

        @Test
        fun forbiddenWithWrongApiKey() {
            val response = apiGet(
                path = "/authorities/brreg?groups=123",
                headers = mapOf(Pair("X-API-KEY", "wrong-key")))

            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun respondWithAdminRole() {
            val response = apiGet(
                path = "/authorities/brreg?groups=123",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("organization:974760673:admin", response["body"])
        }

        @Test
        fun respondWithWriteRole() {
            val response0 = apiGet(
                path = "/authorities/brreg?groups=321",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response0["status"])
            assertEquals("organization:974760673:write", response0["body"])

            val response1 = apiGet(
                path = "/authorities/brreg?groups=222",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response1["status"])
            assertEquals("organization:974760673:write", response1["body"])
        }

    }

    @Nested
    internal inner class Skatt {

        @Test
        fun forbiddenWithWrongApiKey() {
            val response = apiGet(
                path = "/authorities/skatt?groups=123",
                headers = mapOf(Pair("X-API-KEY", "wrong-key")))

            assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun respondWithAdminRole() {
            val response = apiGet(
                path = "/authorities/skatt?groups=123",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("organization:974761076:admin", response["body"])
        }

        @Test
        fun respondWithWriteRole() {
            val response = apiGet(
                path = "/authorities/skatt?groups=321",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("organization:974761076:write", response["body"])
        }

        @Test
        fun respondWithReadRole() {
            val response = apiGet(
                path = "/authorities/skatt?groups=111",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("organization:974761076:read", response["body"])
        }

    }

}
