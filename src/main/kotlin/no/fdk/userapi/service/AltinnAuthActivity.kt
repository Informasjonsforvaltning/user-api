package no.fdk.userapi.service

import kotlinx.coroutines.*
import no.fdk.userapi.model.AltinnOrganization
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

private val NEW_SERVICE_CODES = listOf("5755", "5756")

@Component
class AltinnAuthActivity(
    private val altinnUserService: AltinnUserService
) : CoroutineScope by CoroutineScope(Executors.newFixedThreadPool(10).asCoroutineDispatcher()) {

    fun getAuthorities(ssn: String): String {
        val resourceRoleTokens: MutableList<String> = mutableListOf()
        val authTasks = listOf(
            async { organizationAuthorities(ssn) },
            async { altinnUserService.deprecatedGetPersonAuthorities(ssn) },
            async { altinnUserService.getSysAdminAuthorities(ssn) }
        )

        runBlocking { authTasks.forEach { resourceRoleTokens.addAll(it.await()) } }

        return resourceRoleTokens.joinToString(",")
    }

    private fun allOrganizations(ssn: String): List<AltinnOrganization> {
        val orgTasks = NEW_SERVICE_CODES.map {
            async { altinnUserService.organizationsForService(ssn, it) }
        }
        return runBlocking { orgTasks.flatMap { it.await() } }
    }

    private fun organizationAuthorities(ssn: String): List<String> {
        val rightsTasks = allOrganizations(ssn).map {
            async { altinnUserService.authForOrganization(ssn, it) }
        }
        return runBlocking { rightsTasks.flatMap { it.await() } }
    }

}
