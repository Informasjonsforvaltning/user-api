package no.fdk.userapi.controller

import no.fdk.userapi.service.AltinnUserService
import no.fdk.userapi.service.TermsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private fun isPid(username: String): Boolean =
    username.matches("^\\d{11}$".toRegex())

@RestController
@RequestMapping(value = ["/authorities"])
class AuthoritiesController (
    private val altinnUserService: AltinnUserService,
    private val termsService: TermsService
) {

    @GetMapping(value = ["/{id}"])
    fun getAuthorities(@PathVariable id: String): ResponseEntity<String> {
        return if (isPid(id)) {
            altinnUserService.getAuthorities(id)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    @GetMapping(value = ["/terms"])
    fun mapAuthoritiesByAcceptedTerms(@RequestParam authorities: String): ResponseEntity<String> =
        ResponseEntity(termsService.mapAuthoritiesByAcceptedTerms(authorities), HttpStatus.OK)

}