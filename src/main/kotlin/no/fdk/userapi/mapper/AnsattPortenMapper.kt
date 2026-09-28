package no.fdk.userapi.mapper

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.fdk.userapi.model.AnsattPortenDetails
import no.fdk.userapi.model.AnsattPortenParty
import java.util.Base64

private val objectMapper = jacksonObjectMapper()

fun parseAnsattPortenParties(details: String): List<AnsattPortenParty> {
    val json = String(decodeBase64(details), Charsets.UTF_8)
    val node = objectMapper.readTree(json)
    val detailsList = when {
        node.isArray -> objectMapper.convertValue(node, object : TypeReference<List<AnsattPortenDetails>>() {})
        else -> listOf(objectMapper.treeToValue(node, AnsattPortenDetails::class.java))
    }
    return detailsList.flatMap { it.authorizedParties.orEmpty() }
}

fun AnsattPortenParty.orgId(): String? = orgno?.id?.substringAfter(":")?.takeIf { it.isNotBlank() }

private fun decodeBase64(encoded: String): ByteArray = runCatching { Base64.getUrlDecoder().decode(encoded) }
    .recoverCatching { Base64.getDecoder().decode(encoded) }
    .getOrThrow()
