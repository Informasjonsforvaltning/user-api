package no.fdk.userapi.adapter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.mapper.toOrganization
import no.fdk.userapi.mapper.toPerson
import no.fdk.userapi.model.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL

private val logger = LoggerFactory.getLogger(AltinnAdapter::class.java)

@Service
class AltinnAdapter(private val hostProperties: HostProperties) {

    private fun getReportees(socialSecurityNumber: String, serviceCode: String): List<AltinnSubject?> {
        val url = URL("${hostProperties.altinnProxyHost}/api/serviceowner/reportees?ForceEIAuthentication&subject=$socialSecurityNumber&servicecode=$serviceCode&serviceedition=1&\$top=1000")
        return try {
            val subjects: List<AltinnSubject?> = jacksonObjectMapper().readValue(url)

            subjects.forEach { logger.debug("altinn subject for serviceCode $serviceCode: $it") }

            subjects
        } catch (ex: Exception) {
            logger.error("Unable to get reportees from Altinn", ex)
            emptyList()
        }
    }

    fun getPerson(socialSecurityNumber: String, serviceCode: String): AltinnPerson? {
        val reportees = getReportees(socialSecurityNumber, serviceCode)
            .filterNotNull()

        reportees.forEach { logger.debug("altinn reportee for serviceCode $serviceCode: $it") }

        return extractPersonSubject(socialSecurityNumber, reportees)
            ?.toPerson(extractOrganizations(reportees))
    }

    fun getRights(socialSecurityNumber: String, orgNumber: String): AltinnRightsResponse? {
        val url = URL("${hostProperties.altinnProxyHost}/api/serviceowner/authorization/rights?ForceEIAuthentication&subject=${socialSecurityNumber}&reportee=${orgNumber}")
        return try {
            val rightsResponse: AltinnRightsResponse? = jacksonObjectMapper().readValue(url)

            logger.debug("altinn rights response for org $orgNumber: $rightsResponse")

            rightsResponse
        } catch (ex: Exception) {
            logger.error("Unable to get rights from Altinn", ex)
            null
        }
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