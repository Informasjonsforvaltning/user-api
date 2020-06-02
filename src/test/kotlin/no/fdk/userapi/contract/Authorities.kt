package no.fdk.userapi.contract

import no.fdk.userapi.UserApiApplication
import no.fdk.userapi.utils.SSO_KEY
import no.fdk.userapi.utils.WiremockContext
import no.fdk.userapi.utils.apiGet
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

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
        fun respondWithNotFoundWhenNotFoundInAltinn() {
            val response = apiGet(
                path = "/authorities/altinn/12345678901",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun respondWithBothSysAdminAndOrgAdminWhenInAdminList() {
            val response = apiGet(
                path = "/authorities/altinn/10987654321",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("organization:920210023:admin,system:root:admin", response["body"])
        }

        @Test
        fun respondWithAllOrgAdminWhenInAssociatedWithSeveralOrganizations() {
            val response = apiGet(
                path = "/authorities/altinn/11223344556",
                headers = mapOf(Pair("X-API-KEY", SSO_KEY)))

            assertEquals(HttpStatus.OK.value(), response["status"])
            assertEquals("organization:910258028:admin,organization:123456789:admin", response["body"])
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
            assertEquals("organization:123456789:admin,system:root:admin,organization:910258028:read", response["body"])
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

}
