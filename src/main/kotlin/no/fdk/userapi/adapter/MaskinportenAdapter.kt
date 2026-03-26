package no.fdk.userapi.adapter

import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import no.fdk.userapi.configuration.HostProperties
import no.fdk.userapi.configuration.SecurityProperties
import no.fdk.userapi.model.TokenResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import java.time.Duration

private val logger = LoggerFactory.getLogger(MaskinportenAdapter::class.java)

@Service
class MaskinportenAdapter(
    private val hostProperties: HostProperties,
    private val securityProperties: SecurityProperties
) {
    private val baseUrl: String?
        get() = hostProperties.maskinportenApiHost

    private val webClient: WebClient? by lazy {
        baseUrl?.let { url ->
            WebClient.builder()
                .baseUrl(url)
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.create(
                            ConnectionProvider.builder("maskinporten")
                                .maxConnections(20)
                                .pendingAcquireTimeout(Duration.ofSeconds(10))
                                .build()
                        )
                            .responseTimeout(Duration.ofSeconds(15))
                            .doOnConnected { conn ->
                                conn.addHandlerLast(ReadTimeoutHandler(15))
                                    .addHandlerLast(WriteTimeoutHandler(15))
                            }
                    )
                )
                .build()
        }
    }

    suspend fun getToken(scope: String? = null): TokenResponse? = withContext(Dispatchers.IO) {
        val client = webClient
        if (client == null) {
            logger.debug("Maskinporten API host not configured, skipping token request")
            return@withContext null
        }
        val scopeParam = scope?.takeIf { it.isNotBlank() }
            ?: hostProperties.maskinportenScope?.takeIf { it.isNotBlank() }
        return@withContext try {
            var uriSpec = client.get().uri { builder ->
                builder.path("/api/maskinporten/token")
                scopeParam?.let { builder.queryParam("scope", it) }
                builder.build()
            }
            securityProperties.maskinportenApiKey?.takeIf { it.isNotBlank() }?.let { key ->
                uriSpec = uriSpec.header("X-API-Key", key)
            }
            uriSpec
                .retrieve()
                .bodyToMono(TokenResponse::class.java)
                .awaitSingle()
        } catch (ex: Exception) {
            logger.error("Failed to get token from Maskinporten API", ex)
            null
        }
    }
}
