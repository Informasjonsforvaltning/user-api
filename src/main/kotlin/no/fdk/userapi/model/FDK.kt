package no.fdk.userapi.model

data class UserFDK (
    val id: String,
    val firstName: String?,
    val lastName: String?
)

data class RoleFDK (
    val resourceType: ResourceType,
    val resourceId: String,
    val role: Role
) {
    override fun toString(): String =
        "${resourceType.value}:$resourceId:${role.value}"

    enum class ResourceType(val value: String) {
        System("system"),
        Organization("organization"),
        Invalid("invalid")
    }

    enum class Role(val value: String) {
        Admin("admin"),
        Read("read"),
        Invalid("invalid")
    }

    companion object {
        val ROOT_ADMIN = RoleFDK(ResourceType.System, "root", Role.Admin)
        val INVALID = RoleFDK(ResourceType.Invalid, "unknown", Role.Invalid)
    }

}