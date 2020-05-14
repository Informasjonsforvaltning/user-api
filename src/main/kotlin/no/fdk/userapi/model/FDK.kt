package no.fdk.userapi.model

data class UserFDK (
    val id: String?,
    val firstName: String?,
    val lastName: String?
)

class RoleFDK (
    private val resourceType: ResourceType,
    private val resourceId: String,
    private val role: Role
) {
    override fun toString(): String {
        return "$resourceType:$resourceId:$role"
    }

    enum class ResourceType {
        System, Organization
    }

    enum class Role {
        Admin, Read
    }

    companion object {
        val ROOT_ADMIN = RoleFDK(ResourceType.System, "root", Role.Admin)
    }

}