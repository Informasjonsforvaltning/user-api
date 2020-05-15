package no.fdk.userapi.service

import no.fdk.userapi.mapper.fdkRoleFromAuthString
import no.fdk.userapi.model.AcceptedTerms
import no.fdk.userapi.model.OrgAcceptations
import no.fdk.userapi.model.RoleFDK
import no.fdk.userapi.repository.OrgTermsRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TermsService (
    private val orgTermsRepository: OrgTermsRepository
) {

    fun getOrgTerms(orgnr: String): OrgAcceptations? =
        orgTermsRepository.findByIdOrNull(orgnr)

    fun acceptOrgTerms(orgnr: String, acceptor: String): OrgAcceptations {
        val acceptation = AcceptedTerms(acceptor, LocalDate.now())
        val orgAcceptations = orgTermsRepository
            .findByIdOrNull(orgnr)
            ?.let { OrgAcceptations(orgnr, it.acceptations.plus(acceptation)) }
            ?: OrgAcceptations(orgnr, listOf(acceptation))

        orgTermsRepository.save(orgAcceptations)

        return orgAcceptations
    }

    fun deleteOrgTerms(orgnr: String) =
        orgTermsRepository.deleteById(orgnr)

    fun mapAuthoritiesByAcceptedTerms(authorities: String): String =
        authorities.split(",")
            .map { fdkRoleFromAuthString(it) }
            .map {
                if (it.resourceType != RoleFDK.ResourceType.Organization
                    || orgTermsRepository.findByIdOrNull(it.resourceId) != null) {
                    it
                } else it.copy(resourceType = RoleFDK.ResourceType.Invalid)
            }
            .joinToString(",")

}