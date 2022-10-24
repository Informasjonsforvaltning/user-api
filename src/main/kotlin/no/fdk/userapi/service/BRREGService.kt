package no.fdk.userapi.service

import no.fdk.userapi.configuration.BRREGProperties
import no.fdk.userapi.model.RoleFDK
import org.springframework.stereotype.Service

@Service
class BRREGService(
    private val brregProperties: BRREGProperties
) {

    fun getAuthorities(): String =
        RoleFDK(RoleFDK.ResourceType.Organization, brregProperties.orgnr, RoleFDK.Role.Read)
            .toString()

}
