package no.fdk.userapi.security;

import no.fdk.userapi.model.RoleFDK
import no.fdk.userapi.model.RoleFDK.Companion.ROOT_ADMIN
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class EndpointPermissions{

    fun hasOrgReadPermission(jwt: Jwt, orgnr: String): Boolean {
        val writeRole = RoleFDK(RoleFDK.ResourceType.Organization, orgnr, RoleFDK.Role.Admin)
        val readRole = RoleFDK(RoleFDK.ResourceType.Organization, orgnr, RoleFDK.Role.Read)

        val authorities: String? = jwt.claims["authorities"] as? String

        return if (authorities == null) false
        else authorities.contains(writeRole.toString()) || authorities.contains(readRole.toString())
    }

    fun hasOrgWritePermission(jwt: Jwt, orgnr: String): Boolean {
        val writeRole = RoleFDK(RoleFDK.ResourceType.Organization, orgnr, RoleFDK.Role.Admin)

        val authorities: String? = jwt.claims["authorities"] as? String

        return authorities?.contains(writeRole.toString()) ?: false
    }

    fun hasAdminPermission(jwt: Jwt): Boolean {
        val authorities: String? = jwt.claims["authorities"] as? String

        return authorities?.contains(ROOT_ADMIN.toString()) ?: false
    }
}