package no.fdk.userapi.adapter

import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.dto.AltinnOrganization
import no.fdk.userapi.dto.AltinnPerson
import no.fdk.userapi.dto.AltinnSubject
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class AltinnAdapter(private val hostProperties: HostProperties) {

    private fun getReportees(socialSecurityNumber: String): List<AltinnSubject?> {
        val restTemplate = RestTemplate()
        val reporteesUrlTemplate = "${hostProperties.altinnProxyHost}/api/serviceowner/reportees?ForceEIAuthentication&subject={subject}&servicecode=4814&serviceedition=1&\$top=1000"
        val params = Collections.singletonMap("subject", socialSecurityNumber)
        return restTemplate.exchange<List<AltinnSubject?>?>(
            reporteesUrlTemplate,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<AltinnSubject?>?>() {}, params
        ).body ?: emptyList()
    }

    fun getPerson(socialSecurityNumber: String): Optional<AltinnPerson> {
        val reportees = getReportees(socialSecurityNumber)
        return extractPersonSubject(socialSecurityNumber, reportees)
            ?.let { Optional.of(AltinnPerson(it, extractOrganizations(reportees))) }
            ?: Optional.empty()
    }

    companion object {
        private fun extractPersonSubject(socialSecurityNumber: String, reportees: List<AltinnSubject?>): AltinnSubject? =
            reportees.firstOrNull { it?.type == "Person" && it.socialSecurityNumber == socialSecurityNumber }

        private fun extractOrganizations(reportees: List<AltinnSubject?>): List<AltinnOrganization> {
            return reportees
                .filterNotNull()
                .filter { it.type == "Enterprise" }
                .map { AltinnOrganization(it) }
        }
    }
}