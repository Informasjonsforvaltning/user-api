package no.fdk.userapi.adapter

import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.mapper.toOrganization
import no.fdk.userapi.mapper.toPerson
import no.fdk.userapi.model.AltinnOrganization
import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.AltinnSubject
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class AltinnAdapter(private val hostProperties: HostProperties) {

    private fun getReportees(socialSecurityNumber: String): List<AltinnSubject?> {
        val restTemplate = RestTemplate()
        val reporteesUrlTemplate = "${hostProperties.altinnProxyHost}/api/serviceowner/reportees?ForceEIAuthentication&subject={subject}&servicecode=4814&serviceedition=1&\$top=1000"
        val params = mapOf("subject" to socialSecurityNumber)
        return restTemplate.exchange<List<AltinnSubject?>?>(
            reporteesUrlTemplate,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<AltinnSubject?>?>() {}, params
        ).body ?: emptyList()
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