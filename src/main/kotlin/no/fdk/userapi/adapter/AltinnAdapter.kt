package no.fdk.userapi.adapter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.mapper.toOrganization
import no.fdk.userapi.mapper.toPerson
import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.AltinnSubject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL

private val logger = LoggerFactory.getLogger(AltinnAdapter::class.java)

@Service
class AltinnAdapter(private val hostProperties: HostProperties) {

    private fun getReportees(socialSecurityNumber: String): List<AltinnSubject?> {
        val url = URL("${hostProperties.altinnProxyHost}/api/serviceowner/reportees?ForceEIAuthentication&subject=$socialSecurityNumber&servicecode=4814&serviceedition=1&\$top=1000")
        return try {
            jacksonObjectMapper().readValue(url)
        } catch (ex: Exception) {
            logger.error("Unable to get reportees from Altinn")
            emptyList()
        }
    }

    fun getPerson(socialSecurityNumber: String): AltinnPerson? {
        val reportees = getReportees(socialSecurityNumber)
        return extractPersonSubject(socialSecurityNumber, reportees)
            ?.toPerson(extractOrganizations(reportees))
    }

    companion object {
        private fun extractPersonSubject(socialSecurityNumber: String, reportees: List<AltinnSubject?>): AltinnSubject? =
            reportees.firstOrNull { it?.type == "Person" && it.socialSecurityNumber == socialSecurityNumber }

        private fun extractOrganizations(reportees: List<AltinnSubject?>): List<AltinnOrganization> {
            return reportees
                .filterNotNull()
                .filter { it.type == "Enterprise" }
                .map { it.toOrganization() }
        }
    }
}