package no.fdk.userapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class UserFDK (
    val id: String,
    val firstName: String?,
    val lastName: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrgAcceptation (
    val orgId: String,
    val acceptedVersion: String
)

class RoleFDK (
    private val resourceType: ResourceType,
    private val resourceId: String,
    private val role: Role
) {
    override fun toString(): String {
        return "${resourceType.value}:$resourceId:${role.value}"
    }

    enum class ResourceType(val value: String) {
        System("system"),
        Organization("organization")
    }

    enum class Role(val value: String) {
        Admin("admin"),
        Write("write"),
        Read("read")
    }

    companion object {
        val ROOT_ADMIN = RoleFDK(ResourceType.System, "root", Role.Admin)
    }

    override fun equals(other: Any?): Boolean =
        other is RoleFDK
            && resourceType == other.resourceType
            && resourceId == other.resourceId
            && role == other.role

    override fun hashCode(): Int {
        var result = resourceType.hashCode()
        result = 31 * result + resourceId.hashCode()
        result = 31 * result + role.hashCode()
        return result
    }

}
