package no.fdk.userapi.model

data class UserFDK (
    val id: String,
    val firstName: String?,
    val lastName: String?
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

}