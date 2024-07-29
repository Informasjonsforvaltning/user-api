package no.fdk.userapi.adapter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.mapper.toOrganization
import no.fdk.userapi.mapper.toPerson
import no.fdk.userapi.model.*
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.get
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.net.URI
import java.time.Duration

private val logger = LoggerFactory.getLogger(AltinnAdapter::class.java)
private val objectMapper = jacksonObjectMapper()

@Service
class AltinnAdapter(private val hostProperties: HostProperties, private val cacheManager: CacheManager) {
    private val webClient = WebClient.builder()
        .clientConnector(ReactorClientHttpConnector(
            HttpClient.create(
                ConnectionProvider.builder("custom")
                    .maxConnections(100)
                    .pendingAcquireTimeout(Duration.ofSeconds(60))
                    .build())
            .responseTimeout(Duration.ofSeconds(30))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(30))
                    .addHandlerLast(WriteTimeoutHandler(30))
            }))
        .build()

    private suspend fun <T: Any> fetchJson(uri: URI, typeRef: TypeReference<T>): T? = withContext(Dispatchers.IO) {
        val cache = cacheManager["altinn"]
        return@withContext cache?.get(uri.toString())?.let { it.get() as T? } ?: run {
            logger.debug("Fetching JSON")
            val response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingle()

            val jsonObject = objectMapper.readValue(response, typeRef)
            cache?.put(uri.toString(), jsonObject)
            jsonObject
        }
    }

    private suspend fun getReportees(socialSecurityNumber: String, serviceCode: String): List<AltinnSubject?> {
        logger.debug("Fetching reportees from Altinn")
        val uri = URI("${hostProperties.altinnProxyHost}/api/serviceowner/reportees?ForceEIAuthentication&subject=$socialSecurityNumber&servicecode=$serviceCode&serviceedition=1&\$top=1000")
        return try {
            fetchJson(uri, object: TypeReference<List<AltinnSubject>>() {}) ?: emptyList()
        } catch (ex: Exception) {
            logger.error("Unable to get reportees from Altinn", ex)
            emptyList()
        }
    }

    suspend fun getPerson(socialSecurityNumber: String, serviceCode: String): AltinnPerson? {
        val reportees = getReportees(socialSecurityNumber, serviceCode)
            .filterNotNull()

        return extractPersonSubject(socialSecurityNumber, reportees)
            ?.toPerson(extractOrganizations(reportees))
    }

    suspend fun getRights(socialSecurityNumber: String, orgNumber: String): AltinnRightsResponse? {
        logger.debug("Fetching rights from Altinn")
        val uri =
            URI("${hostProperties.altinnProxyHost}/api/serviceowner/authorization/rights?ForceEIAuthentication&subject=${socialSecurityNumber}&reportee=${orgNumber}&%24filter=ServiceCode%20eq%20%275755%27%20or%20ServiceCode%20eq%20%275756%27%20or%20ServiceCode%20eq%20%275977%27")
        return try {
            fetchJson(uri, object: TypeReference<AltinnRightsResponse>() {})
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
