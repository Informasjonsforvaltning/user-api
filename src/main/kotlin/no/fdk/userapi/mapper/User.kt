import no.fdk.userapi.model.AltinnPerson
import no.fdk.userapi.model.UserFDK

fun AltinnPerson.toUserFDK(): UserFDK {
    val names: List<String> = name
        ?.split("\\s+".toRegex())
        ?.toList()
        ?: emptyList()

    return UserFDK(
        id = socialSecurityNumber,
        firstName = names.subList(0, names.size - 1).joinToString(" "),
        lastName = names.last()
    )
}