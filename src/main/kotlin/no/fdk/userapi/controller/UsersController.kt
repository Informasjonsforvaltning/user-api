package no.fdk.userapi.controller

import no.fdk.userapi.service.AltinnUserService
import no.fdk.userapi.model.UserFDK
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.fdk.userapi.mapper.toUserFDK
import org.springframework.web.bind.annotation.GetMapping

@RestController
@RequestMapping(value = ["/users"])
class UsersController (
    private val altinnUserService: AltinnUserService
) {

    /**
     * This endpoint is active user database
     * Currently We do not store permanently users, instead we get dynamically the user data and privileges from Altinn.
     */
    @GetMapping(value = ["/{id}"])
    fun getUserInfo(@PathVariable id: String): ResponseEntity<UserFDK> {
        return altinnUserService.getUser(id)
            ?.toUserFDK()
            ?.let { ResponseEntity(it, HttpStatus.OK) }
            ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UsersController::class.java)
    }

}