package no.fdk.userapi.controller

import no.fdk.userapi.model.OrgAcceptations
import no.fdk.userapi.service.TermsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/terms"])
class TermsController (private val termsService: TermsService){

    @GetMapping(value = ["/org/{id}"])
    fun getOrgTerms(@PathVariable id: String): ResponseEntity<OrgAcceptations> {
        // org read permission
        return termsService.getOrgTerms(id)
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @PutMapping(value = ["/org/{id}"])
    fun acceptOrgTerms(@PathVariable id: String): ResponseEntity<OrgAcceptations> {
        // org write permission
        return ResponseEntity(termsService.acceptOrgTerms(id, "name from token"), HttpStatus.OK)
    }

    @DeleteMapping(value = ["/org/{id}"])
    fun deleteOrgTerms(@PathVariable id: String): ResponseEntity<Unit> {
        // sys admin permission
        termsService.deleteOrgTerms(id)
        return ResponseEntity(HttpStatus.OK)
    }
}