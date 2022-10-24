package no.fdk.userapi.service

import no.fdk.userapi.adapter.TermsAdapter
import no.fdk.userapi.configuration.BRREGProperties
import org.springframework.stereotype.Service

@Service
class TermsService(
    private val termsAdapter: TermsAdapter,
    private val altinnAuthActivity: AltinnAuthActivity,
    private val brregProperties: BRREGProperties
) {

    fun getOrgTermsAltinn(id: String): String =
        altinnAuthActivity.getOrganizationsForTerms(id)
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

    private fun Pair<String, String?>.toTermsString(): String? =
        if (second != "0.0.0") "${first}:${second}" else null

}
