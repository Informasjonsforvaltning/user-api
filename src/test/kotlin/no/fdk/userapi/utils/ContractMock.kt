package no.fdk.userapi.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private val mockserver = WireMockServer(LOCAL_SERVER_PORT)

fun startMockServer() {
    if(!mockserver.isRunning) {
        mockserver.stubFor(
            get(urlEqualTo("/ping"))
                .willReturn(ok()))

        mockserver.stubFor(
            get(urlPathMatching("/api/maskinporten/token.*"))
                .willReturn(okJson("""{"access_token":"contract-test-token","token_type":"Bearer","expires_in":3600,"scope":"altinn:accessmanagement/authorizedparties.resourceowner"}""")))

        mockserver.stubFor(
            post(urlPathEqualTo("/accessmanagement/api/v1/resourceowner/authorizedparties"))
                .withRequestBody(containing("12345678901"))
                .willReturn(okJson("[]")))
        mockserver.stubFor(
            post(urlPathEqualTo("/accessmanagement/api/v1/resourceowner/authorizedparties"))
                .withRequestBody(containing("10987654321"))
                .willReturn(okJson(AUTHORIZED_PARTIES_0)))
        mockserver.stubFor(
            post(urlPathEqualTo("/accessmanagement/api/v1/resourceowner/authorizedparties"))
                .withRequestBody(containing("11223344556"))
                .willReturn(okJson(AUTHORIZED_PARTIES_1)))

        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=920210023"))
                .willReturn(okJson("""[{"orgId": "920210023", "acceptedVersion":  "1.2.3"}]""")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=910258028"))
                .willReturn(okJson("""[{"orgId": "910258028", "acceptedVersion":  "1.0.0"}]""")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=123456789,910258028"))
                .willReturn(okJson("""[{"orgId": "910258028", "acceptedVersion":  "1.0.0"}]""")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=910258028,123456789"))
                .willReturn(okJson("""[{"orgId": "910258028", "acceptedVersion":  "1.0.0"}]""")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=123456789,920210023"))
                .willReturn(okJson("""[{"orgId": "920210023", "acceptedVersion":  "1.2.3"}]""")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=920210023,123456789"))
                .willReturn(okJson("""[{"orgId": "920210023", "acceptedVersion":  "1.2.3"}]""")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=123456789"))
                .willReturn(okJson("[]")))

        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=971183675"))
                .willReturn(okJson("[]")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=987592567"))
                .willReturn(okJson("""[{"orgId": "987592567", "acceptedVersion":  "1.0.1"}]""")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=923954791"))
                .willReturn(okJson("""[{"orgId": "923954791", "acceptedVersion":  "12.16.11"}]""")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=974760673"))
                .willReturn(okJson("""[{"orgId": "974760673", "acceptedVersion":  "1.0.1"}]""")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org?organizations=974761076"))
                .willReturn(okJson("""[{"orgId": "974761076", "acceptedVersion":  "1.1.1"}]""")))

        mockserver.start()
    }
}

abstract class WiremockContext {
    companion object {

        private val logger = LoggerFactory.getLogger(WiremockContext::class.java)

        init {

            startMockServer()

            try {
                val con = URL("http://localhost:5555/ping").openConnection() as HttpURLConnection
                con.connect()
                if (con.responseCode != 200) {
                    logger.debug("Ping to mock server failed")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

}
