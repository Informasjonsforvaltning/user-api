package no.fdk.userapi.controller

import no.fdk.userapi.mapper.isPid
import no.fdk.userapi.mapper.mapAuthoritiesFromDifiRole
import no.fdk.userapi.service.AltinnUserService
import no.fdk.userapi.service.EndpointPermissions
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(value = ["/authorities"])
class AuthoritiesController (
    private val altinnUserService: AltinnUserService,
    private val endpointPermissions: EndpointPermissions
) {

    @GetMapping(value = ["/altinn/{id}"])
    fun getAuthorities(httpServletRequest: HttpServletRequest, @PathVariable id: String): ResponseEntity<String> =
        when {
            !endpointPermissions.isFromFDKCluster(httpServletRequest) -> ResponseEntity(HttpStatus.FORBIDDEN)
            !isPid(id) -> ResponseEntity(HttpStatus.BAD_REQUEST)
            else -> {
                altinnUserService.getAuthorities(id)
                    ?.let { ResponseEntity(it, HttpStatus.OK) }
                    ?: ResponseEntity(HttpStatus.NOT_FOUND)
            }
        }

    @GetMapping(value = ["/difi"])
    fun getDifiAuthorities(
        httpServletRequest: HttpServletRequest,
        @RequestParam(value = "roles", required = true) roles: List<String>,
        @RequestParam(value = "orgs", required = true) orgs: List<String>
    ): ResponseEntity<String> =
        if (endpointPermissions.isFromFDKCluster(httpServletRequest)) {
            ResponseEntity(mapAuthoritiesFromDifiRole(roles, orgs), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}
