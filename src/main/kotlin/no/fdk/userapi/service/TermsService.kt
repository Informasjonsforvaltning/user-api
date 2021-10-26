package no.fdk.userapi.service

import no.fdk.userapi.adapter.TermsAdapter
import no.fdk.userapi.mapper.orgNameToNumber
import org.springframework.stereotype.Service

@Service
class TermsService(
    private val termsAdapter: TermsAdapter,
    private val altinnAuthActivity: AltinnAuthActivity
) {

    fun getOrgTermsAltinn(id: String): String =
        altinnAuthActivity.getOrganizationsforTerms(id)
            .mapNotNull { it.organizationNumber }
            .joinToString(",") { "$it:${termsAdapter.orgAcceptedTermsVersion(it)}" }

    fun getOrgTermsDifi(orgs: List<String>): String =
        orgs.joinToString(",") { "$it:${termsAdapter.orgAcceptedTermsVersion(it)}" }

    fun getOrgTermsOk(orgNames: List<String>): String =
        orgNames.mapNotNull { orgNameToNumber(it) }
            .joinToString(",") { "$it:${termsAdapter.orgAcceptedTermsVersion(it)}" }

}