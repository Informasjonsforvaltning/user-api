package no.fdk.userapi.controller

import no.fdk.userapi.mapper.mapAuthoritiesFromDifiRole
import no.fdk.userapi.service.AltinnUserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/authorities"])
class AuthoritiesController (
    private val altinnUserService: AltinnUserService
) {

    @GetMapping(value = ["/altinn/{id}"])
    fun getAuthorities(@PathVariable id: String): ResponseEntity<String> {
        return if (isPid(id)) {
            altinnUserService.getAuthorities(id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    @GetMapping(value = ["/difi"])
    fun getDifiAuthorities(
        @RequestParam(value = "roles", required = true) roles: List<String>,
        @RequestParam(value = "orgs", required = true) orgs: List<String>
    ): ResponseEntity<String> =
        ResponseEntity(mapAuthoritiesFromDifiRole(roles, orgs), HttpStatus.OK)

}

private fun isPid(username: String): Boolean =
    username.matches("^\\d{11}$".toRegex())
