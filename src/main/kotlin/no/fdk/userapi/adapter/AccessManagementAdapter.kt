package no.fdk.userapi.adapter

import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.model.AuthorizedParty
import no.fdk.userapi.model.AuthorizedPartyRequest
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import java.time.Duration

private val logger = LoggerFactory.getLogger(AccessManagementAdapter::class.java)

private const val SUBJECT_TYPE_PERSON = "urn:altinn:person:identifier-no"

@Service
class AccessManagementAdapter(
    private val hostProperties: HostProperties,
    private val maskinportenAdapter: MaskinportenAdapter
) {
    private val baseUrl: String?
        get() = hostProperties.altinnAccessManagementHost

    private val webClient: WebClient? by lazy {
        baseUrl?.let { url ->
            WebClient.builder()
                .baseUrl(url)
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.create(
                            ConnectionProvider.builder("access-management")
                                .maxConnections(50)
                                .pendingAcquireTimeout(Duration.ofSeconds(15))
                                .build()
                        )
                            .responseTimeout(Duration.ofSeconds(30))
                            .doOnConnected { conn ->
                                conn.addHandlerLast(ReadTimeoutHandler(30))
                                    .addHandlerLast(WriteTimeoutHandler(30))
                            }
                    )
                )
                .build()
        }
    }

    suspend fun getAuthorizedParties(ssn: String): List<AuthorizedParty>? = withContext(Dispatchers.IO) {
        val client = webClient
        if (client == null) {
            logger.debug("Access Management API host not configured")
            return@withContext null
        }
        val token = maskinportenAdapter.getToken()?.accessToken
        if (token == null) {
            logger.warn("No Maskinporten token available for Access Management call")
            return@withContext null
        }
        val request = AuthorizedPartyRequest(
            type = SUBJECT_TYPE_PERSON,
            value = ssn
        )
        return@withContext try {
            logger.debug("Fetching authorized parties from Altinn Access Management")
            client.post()
                .uri { it.path("/resourceowner/authorizedparties").queryParam("includeAltinn3", true).queryParam("includeResources", true).build() }
                .header("Authorization", "Bearer $token")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<AuthorizedParty>>() {})
                .awaitSingle()
        } catch (ex: Exception) {
            logger.error("Failed to get authorized parties from Access Management API", ex)
            null
        }
    }
}
