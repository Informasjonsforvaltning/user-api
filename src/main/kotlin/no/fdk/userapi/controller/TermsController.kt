package no.fdk.userapi.controller

import no.fdk.userapi.mapper.isPid
import no.fdk.userapi.service.EndpointPermissions
import no.fdk.userapi.service.TermsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping(value = ["/terms"])
class TermsController(
    private val termsService: TermsService,
    private val endpointPermissions: EndpointPermissions
) {

    @GetMapping(value = ["/altinn/{id}"])
    suspend fun getOrgTermsAltinn(exchange: ServerWebExchange, @PathVariable id: String): ResponseEntity<String> =
        when {
            !endpointPermissions.isFromFDKCluster(exchange.request) -> ResponseEntity(HttpStatus.FORBIDDEN)
            !isPid(id) -> ResponseEntity(HttpStatus.BAD_REQUEST)
            else -> ResponseEntity(termsService.getOrgTermsAltinn(id), HttpStatus.OK)
        }

    @GetMapping(value = ["/difi"])
    fun getOrgTermsDifi(
        exchange: ServerWebExchange,
        @RequestParam(value = "orgs", required = true) orgs: List<String>
    ): ResponseEntity<String> =
        if(endpointPermissions.isFromFDKCluster(exchange.request)) {
            ResponseEntity(termsService.getOrgTermsDifi(orgs), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @GetMapping(value = ["/brreg"])
    fun getOrgTermsBRREG(exchange: ServerWebExchange): ResponseEntity<String> =
        if(endpointPermissions.isFromFDKCluster(exchange.request)) {
            ResponseEntity(termsService.getOrgTermsBRREG(), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @GetMapping(value = ["/skatt"])
    fun getOrgTermsSkatt(exchange: ServerWebExchange): ResponseEntity<String> =
        if(endpointPermissions.isFromFDKCluster(exchange.request)) {
            ResponseEntity(termsService.getOrgTermsSkatt(), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}
