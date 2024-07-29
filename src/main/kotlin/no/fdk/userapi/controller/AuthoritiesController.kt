package no.fdk.userapi.controller

import no.fdk.userapi.mapper.isPid
import no.fdk.userapi.mapper.mapAuthoritiesFromDifiRole
import no.fdk.userapi.service.AltinnUserService
import no.fdk.userapi.service.BRREGService
import no.fdk.userapi.service.EndpointPermissions
import no.fdk.userapi.service.SkattService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping(value = ["/authorities"])
class AuthoritiesController (
    private val altinnUserService: AltinnUserService,
    private val endpointPermissions: EndpointPermissions,
    private val brregService: BRREGService,
    private val skattService: SkattService
) {

    @GetMapping(value = ["/altinn/{id}"])
    suspend fun getAuthorities(exchange: ServerWebExchange, @PathVariable id: String): ResponseEntity<String> =
        when {
            !endpointPermissions.isFromFDKCluster(exchange.request) -> ResponseEntity(HttpStatus.FORBIDDEN)
            !isPid(id) -> ResponseEntity(HttpStatus.BAD_REQUEST)
            else -> ResponseEntity(altinnUserService.getAuthorities(id), HttpStatus.OK)
        }

    @GetMapping(value = ["/difi"])
    fun getDifiAuthorities(
        exchange: ServerWebExchange,
        @RequestParam(value = "roles", required = true) roles: List<String>,
        @RequestParam(value = "orgs", required = true) orgs: List<String>
    ): ResponseEntity<String> =
        if (endpointPermissions.isFromFDKCluster(exchange.request)) {
            ResponseEntity(mapAuthoritiesFromDifiRole(roles, orgs), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @GetMapping(value = ["/brreg"])
    fun getBRREGAuthorities(
        exchange: ServerWebExchange,
        @RequestParam(value = "groups", required = true) groups: List<String>
    ): ResponseEntity<String> {
        return if (endpointPermissions.isFromFDKCluster(exchange.request)) {
            ResponseEntity(brregService.getAuthorities(groups), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)
    }

    @GetMapping(value = ["/skatt"])
    fun getSkattAuthorities(
        exchange: ServerWebExchange,
        @RequestParam(value = "groups", required = true) groups: List<String>
    ): ResponseEntity<String> {
        return if (endpointPermissions.isFromFDKCluster(exchange.request)) {
            ResponseEntity(skattService.getAuthorities(groups), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)
    }

}
