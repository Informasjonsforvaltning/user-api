package no.fdk.userapi.adapter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.mapper.toOrganization
import no.fdk.userapi.mapper.toPerson
import no.fdk.userapi.model.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

private val logger = LoggerFactory.getLogger(AltinnAdapter::class.java)

@Service
class AltinnAdapter(private val hostProperties: HostProperties) {
    private val tenSeconds = 10000

    private fun altinnStream(url: URL): InputStream =
        with(url.openConnection() as HttpURLConnection) {
            connectTimeout = tenSeconds
            connect()
            if (HttpStatus.resolve(responseCode)?.is2xxSuccessful == true) {
                inputStream
            } else {
                throw Exception("altinn connection failed with code $responseCode")
            }
        }

    private fun getReportees(socialSecurityNumber: String, serviceCode: String): List<AltinnSubject?> {
        val url = URL("${hostProperties.altinnProxyHost}/api/serviceowner/reportees?ForceEIAuthentication&subject=$socialSecurityNumber&servicecode=$serviceCode&serviceedition=1&\$top=1000")
        return try {
            jacksonObjectMapper().readValue(altinnStream(url))
        } catch (ex: Exception) {
            logger.error("Unable to get reportees from Altinn", ex)
            emptyList()
        }
    }

    fun getPerson(socialSecurityNumber: String, serviceCode: String): AltinnPerson? {
        val reportees = getReportees(socialSecurityNumber, serviceCode)
            .filterNotNull()

        return extractPersonSubject(socialSecurityNumber, reportees)
            ?.toPerson(extractOrganizations(reportees))
    }

    fun getRights(socialSecurityNumber: String, orgNumber: String): AltinnRightsResponse? =
        try {
            val url = URL("${hostProperties.altinnProxyHost}/api/serviceowner/authorization/rights?ForceEIAuthentication&subject=${socialSecurityNumber}&reportee=${orgNumber}&%24filter=ServiceCode%20eq%20%275755%27%20or%20ServiceCode%20eq%20%275756%27%20or%20ServiceCode%20eq%20%275977%27")
            jacksonObjectMapper().readValue(altinnStream(url))
        } catch (ex: Exception) {
            logger.error("Unable to get rights from Altinn", ex)
            null
        }

    companion object {
        private fun extractPersonSubject(socialSecurityNumber: String, reportees: List<AltinnSubject?>): AltinnSubject? =
            reportees.firstOrNull { it?.type == "Person" && it.socialSecurityNumber == socialSecurityNumber }

        private fun extractOrganizations(reportees: List<AltinnSubject?>): List<AltinnOrganization> {
            return reportees
                .filterNotNull()
                .filter { includeReporteeAsOrganization(it.type) }
                .map { it.toOrganization() }
        }

        private fun includeReporteeAsOrganization(reporteeType: String?): Boolean =
            when (reporteeType) {
                null -> false
                AltinnReporteeType.Person.name -> false
                AltinnReporteeType.Enterprise.name -> true
                AltinnReporteeType.Business.name -> true
                else -> false
            }
    }
}
