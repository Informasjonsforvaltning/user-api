package no.fdk.userapi.controller

import no.fdk.userapi.model.OrgAcceptations
import no.fdk.userapi.security.EndpointPermissions
import no.fdk.userapi.service.TermsService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(TermsController::class.java)

@RestController
@RequestMapping(value = ["/terms"])
class TermsController (
    private val termsService: TermsService,
    private val endpointPermissions: EndpointPermissions
){

    @GetMapping(value = ["/org/{id}"])
    fun getOrgTerms(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): ResponseEntity<OrgAcceptations> =
        if (endpointPermissions.hasOrgReadPermission(jwt, id)) {
            logger.info("Get terms acceptations for organization with id $id")
            termsService.getOrgTerms(id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @PutMapping(value = ["/org/{id}"])
    fun acceptOrgTerms(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): ResponseEntity<OrgAcceptations> =
        if (endpointPermissions.hasOrgWritePermission(jwt, id)) {
            logger.info("Accept terms for organization with id $id")
            termsService.acceptOrgTerms(jwt, id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.BAD_REQUEST)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

    @DeleteMapping(value = ["/org/{id}"])
    fun deleteOrgTerms(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): ResponseEntity<Unit> =
        if (endpointPermissions.hasAdminPermission(jwt)) {
            logger.info("Delete terms acceptations for organization with id $id")
            termsService.deleteOrgTerms(id)
            ResponseEntity(HttpStatus.OK)
        } else ResponseEntity(HttpStatus.FORBIDDEN)

}