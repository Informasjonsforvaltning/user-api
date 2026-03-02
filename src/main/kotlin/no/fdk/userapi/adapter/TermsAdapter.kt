package no.fdk.userapi.adapter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.configuration.SecurityProperties
import no.fdk.userapi.model.OrgAcceptation
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

private val logger = LoggerFactory.getLogger(TermsAdapter::class.java)
private val objectMapper = jacksonObjectMapper()

@Service
class TermsAdapter(
    private val hostProperties: HostProperties,
    private val securityProperties: SecurityProperties,
    private val cacheManager: CacheManager
) {

    private val webClient = WebClient.builder()
        .clientConnector(ReactorClientHttpConnector(
            HttpClient.create(
                ConnectionProvider.builder("custom")
                    .maxConnections(100)
                    .pendingAcquireTimeout(Duration.ofSeconds(60))
                    .build())
                .responseTimeout(Duration.ofSeconds(30))
                .headers { headers -> headers.add("X-API-KEY", securityProperties.userApiKey) }
                .doOnConnected { conn ->
                    conn.addHandlerLast(ReadTimeoutHandler(30))
                        .addHandlerLast(WriteTimeoutHandler(30))
                }))
        .build()

    private suspend fun <T: Any> fetchJson(uri: URI, typeRef: TypeReference<T>): T? = withContext(Dispatchers.IO) {
        val cache = cacheManager["terms"]
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

    suspend fun acceptedTermsForOrganizations(organizations: List<String>): List<String> {
        logger.debug("Fetching terms for organizations")
        val uri = URI("${hostProperties.termsHost}/terms/org?organizations=${organizations.joinToString(",")}")
        val orgAcceptations = try {
            fetchJson(uri, object: TypeReference<List<OrgAcceptation>>() {}) ?: emptyList()
        } catch (ex: Exception) {
            logger.error("Unable to get reportees from Altinn", ex)
            emptyList()
        }

        return orgAcceptations.mapNotNull { it.toTermsString() }
    }

    private fun OrgAcceptation.toTermsString(): String? =
        if (acceptedVersion != "0.0.0") "${orgId}:${acceptedVersion}" else null

}
