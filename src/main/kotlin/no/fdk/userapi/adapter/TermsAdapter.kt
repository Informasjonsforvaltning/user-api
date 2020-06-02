package no.fdk.userapi.adapter

import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.configuration.SecurityProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL

private val logger = LoggerFactory.getLogger(TermsAdapter::class.java)

@Service
class TermsAdapter(
    private val hostProperties: HostProperties,
    private val securityProperties: SecurityProperties
) {

    fun orgAcceptedTermsVersion(organization: String): String {
        val url = URL("${hostProperties.termsHost}/terms/org/$organization/version")
        try {
            with(url.openConnection() as HttpURLConnection) {
                setRequestProperty("X-API-KEY", securityProperties.userApiKey)
                connect()
                if (HttpStatus.resolve(responseCode)?.is2xxSuccessful == true) {
                    inputStream.bufferedReader().use {
                        return it.readText()
                    }
                } else {
                    errorStream.bufferedReader().use {
                        logger.error("Unable to get accepted terms version", it.readText())
                    }
                    return "0.0.0"
                }
            }
        } catch (ex: Exception) {
            logger.error("Unable to get accepted terms version", ex)
            return "0.0.0"
        }
    }

}
