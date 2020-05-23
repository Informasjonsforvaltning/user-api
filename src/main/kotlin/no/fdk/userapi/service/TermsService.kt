package no.fdk.userapi.service

import no.fdk.userapi.adapter.TermsAdapter
import org.springframework.stereotype.Service

@Service
class TermsService(
    private val termsAdapter: TermsAdapter,
    private val altinnUserService: AltinnUserService
) {

    fun getOrgTermsAltinn(id: String): String =
        altinnUserService.getUser(id)
            ?.organizations
            ?.mapNotNull { it.organizationNumber }
            ?.map { "$it:${termsAdapter.orgAcceptedTermsVersion(it)}" }
            ?.joinToString(",")
            ?: ""

    fun getOrgTermsDifi(orgs: List<String>): String =
        orgs.map { "$it:${termsAdapter.orgAcceptedTermsVersion(it)}" }
            .joinToString(",")

}