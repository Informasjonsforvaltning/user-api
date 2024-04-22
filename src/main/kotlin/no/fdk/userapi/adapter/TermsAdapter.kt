package no.fdk.userapi.adapter

import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.configuration.SecurityProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URI

private val logger = LoggerFactory.getLogger(TermsAdapter::class.java)

@Service
class TermsAdapter(
    private val hostProperties: HostProperties,
    private val securityProperties: SecurityProperties
) {
    private val fiveSeconds = 5000

    fun orgAcceptedTermsVersion(organization: String): String {
        val uri = URI("${hostProperties.termsHost}/terms/org/$organization/version")
        try {
            with(uri.toURL().openConnection() as HttpURLConnection) {
                setRequestProperty("X-API-KEY", securityProperties.userApiKey)
                connectTimeout = fiveSeconds
                readTimeout = fiveSeconds
                connect()
                if (HttpStatus.resolve(responseCode)?.is2xxSuccessful == true) {
                    inputStream.bufferedReader().use {
                        return it.readText()
                    }
                } else if (HttpStatus.resolve(responseCode) == HttpStatus.NOT_FOUND) {
                    return "0.0.0"
                } else {
                    logger.error("Unable to get accepted terms version for $organization. Response code: $responseCode", Exception())
                    return "0.0.0"
                }
            }
        } catch (ex: Exception) {
            logger.error("Unable to get accepted terms version for $organization", ex)
            return "0.0.0"
        }
    }

}
