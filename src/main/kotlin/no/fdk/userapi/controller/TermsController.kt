package no.fdk.userapi.controller

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
import javax.servlet.http.HttpServletRequest

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

    @GetMapping(value = ["/oslokommune"])
    fun getOrgTermsOK(
        httpServletRequest: HttpServletRequest,
        @RequestParam(value = "orgnames", required = true) orgNames: List<String>
    ): ResponseEntity<String> =
        if(endpointPermissions.isFromFDKCluster(httpServletRequest)) {
            ResponseEntity(termsService.getOrgTermsOk(orgNames), HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}
