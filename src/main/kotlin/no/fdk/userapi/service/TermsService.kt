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

    suspend fun getOrgTermsAltinn(id: String): String {
        val orgs = altinnUserService.getOrganizationsForTerms(id)
            .asSequence()
            .mapNotNull { it.organizationNumber }
            .distinct()
            .toList()

        return termsAdapter.acceptedTermsForOrganizations(orgs)
            .joinToString(",")
    }

    suspend fun getOrgTermsDifi(orgs: List<String>): String =
        termsAdapter.acceptedTermsForOrganizations(orgs.distinct())
            .joinToString(",")

    suspend fun getOrgTermsBRREG(): String =
        termsAdapter.acceptedTermsForOrganizations(listOf(brregProperties.orgnr))
            .joinToString(",")

    suspend fun getOrgTermsSkatt(): String =
        termsAdapter.acceptedTermsForOrganizations(listOf(skattProperties.orgnr))
            .joinToString(",")

}
