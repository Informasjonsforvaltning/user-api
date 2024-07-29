package no.fdk.userapi.service

import no.fdk.userapi.adapter.TermsAdapter
import no.fdk.userapi.configuration.BRREGProperties
import no.fdk.userapi.configuration.SkattProperties
import org.springframework.stereotype.Service

@Service
class TermsService(
    private val termsAdapter: TermsAdapter,
    private val altinnUserService: AltinnUserService,
    private val brregProperties: BRREGProperties,
    private val skattProperties: SkattProperties
) {

    suspend fun getOrgTermsAltinn(id: String): String =
        altinnUserService.getOrganizationsForTerms(id)
            .asSequence()
            .mapNotNull { it.organizationNumber }
            .distinct()
            .map { Pair(it, termsAdapter.orgAcceptedTermsVersion(it)) }
            .mapNotNull { it.toTermsString() }
            .joinToString(",")

    fun getOrgTermsDifi(orgs: List<String>): String =
        orgs.distinct()
            .map { Pair(it, termsAdapter.orgAcceptedTermsVersion(it)) }
            .mapNotNull { it.toTermsString() }
            .joinToString(",")

    fun getOrgTermsBRREG(): String =
        Pair(brregProperties.orgnr, termsAdapter.orgAcceptedTermsVersion(brregProperties.orgnr))
            .toTermsString() ?: ""

    fun getOrgTermsSkatt(): String =
        Pair(skattProperties.orgnr, termsAdapter.orgAcceptedTermsVersion(skattProperties.orgnr))
            .toTermsString() ?: ""

    private fun Pair<String, String?>.toTermsString(): String? =
        if (second != "0.0.0") "${first}:${second}" else null

}
