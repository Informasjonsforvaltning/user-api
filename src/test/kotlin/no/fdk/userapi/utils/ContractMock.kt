package no.fdk.userapi.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private val mockserver = WireMockServer(LOCAL_SERVER_PORT)

fun startMockServer() {
    if(!mockserver.isRunning) {
        mockserver.stubFor(
            get(urlEqualTo("/ping"))
                .willReturn(aResponse().withStatus(200)))

        mockserver.stubFor(
            get(urlEqualTo("/altinn/api/serviceowner/reportees?ForceEIAuthentication&subject=12345678901&servicecode=4814&serviceedition=1&\$top=1000"))
                .willReturn(aResponse().withStatus(404)))
        mockserver.stubFor(
            get(urlEqualTo("/altinn/api/serviceowner/reportees?ForceEIAuthentication&subject=10987654321&servicecode=4814&serviceedition=1&\$top=1000"))
                .willReturn(aResponse().withStatus(200).withBody(ALTINN_PERSON_0)))
        mockserver.stubFor(
            get(urlEqualTo("/altinn/api/serviceowner/reportees?ForceEIAuthentication&subject=11223344556&servicecode=4814&serviceedition=1&\$top=1000"))
                .willReturn(aResponse().withStatus(200).withBody(ALTINN_PERSON_1)))

        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org/920210023/version"))
                .willReturn(aResponse().withStatus(200).withBody("1.2.3")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org/910258028/version"))
                .willReturn(aResponse().withStatus(200).withBody("1.0.0")))
        mockserver.stubFor(
            get(urlEqualTo("/terms/terms/org/123456789/version"))
                .willReturn(aResponse().withStatus(404)))

        mockserver.start()
    }
}

fun stopMockServer() {

    if (mockserver.isRunning) mockserver.stop()

}

abstract class WiremockContext {
    companion object {

        private val logger = LoggerFactory.getLogger(WiremockContext::class.java)

        init {

            startMockServer()

            try {
                val con = URL("http://localhost:5000/ping").openConnection() as HttpURLConnection
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
