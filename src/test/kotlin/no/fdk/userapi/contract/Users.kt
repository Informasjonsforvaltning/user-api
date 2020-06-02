package no.fdk.userapi.contract

import no.fdk.userapi.UserApiApplication
import no.fdk.userapi.utils.WiremockContext
import no.fdk.userapi.utils.apiGet
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
class Users : WiremockContext() {

    @Test
    fun badRequestOnBadSSN() {
        val response = apiGet("/users/123", emptyMap())
        assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
    }

    @Test
    fun notFoundWhenNotPresentInAltinn() {
        val response = apiGet("/users/12345678901", emptyMap())
        assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }

    @Test
    fun respondWithNameAndSSNWhenFound() {
        val response = apiGet("/users/10987654321", emptyMap())

        val expectedBody = """{"id":"10987654321","firstName":"FIRST NAME","lastName":"LAST"}"""

        assertEquals(HttpStatus.OK.value(), response["status"])
        assertEquals(expectedBody, response["body"])
    }

}
