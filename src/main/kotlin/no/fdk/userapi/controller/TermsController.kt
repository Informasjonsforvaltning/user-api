package no.fdk.userapi.controller

import jakarta.servlet.http.HttpServletRequest
import no.fdk.userapi.mapper.isPid
import no.fdk.userapi.service.EndpointPermissions
import no.fdk.userapi.service.TermsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/terms"])
class TermsController(
    private val termsService: TermsService,
    private val endpointPermissions: EndpointPermissions
) {

    @GetMapping(value = ["/altinn/{id}"])
    fun getOrgTermsAltinn(httpServletRequest: HttpServletRequest, @PathVariable id: String): ResponseEntity<String> =
        when {
            !endpointPermissions.isFromFDKCluster(httpServletRequest) -> ResponseEntity(HttpStatus.FORBIDDEN)
            !isPid(id) -> ResponseEntity(HttpStatus.BAD_REQUEST)
            else -> ResponseEntity(termsService.getOrgTermsAltinn(id), HttpStatus.OK)
        }

    @GetMapping(value = ["/difi"])
    fun getOrgTermsDifi(
        httpServletRequest: HttpServletRequest,
        @RequestParam(value = "orgs", required = true) orgs: List<String>
    ): ResponseEntity<String> =
        if(endpointPermissions.isFromFDKCluster(httpServletRequest)) {
            ResponseEntity(termsService.getOrgTermsDifi(orgs), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @GetMapping(value = ["/brreg"])
    fun getOrgTermsBRREG(httpServletRequest: HttpServletRequest): ResponseEntity<String> =
        if(endpointPermissions.isFromFDKCluster(httpServletRequest)) {
            ResponseEntity(termsService.getOrgTermsBRREG(), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @GetMapping(value = ["/skatt"])
    fun getOrgTermsSkatt(httpServletRequest: HttpServletRequest): ResponseEntity<String> =
        if(endpointPermissions.isFromFDKCluster(httpServletRequest)) {
            ResponseEntity(termsService.getOrgTermsSkatt(), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}
