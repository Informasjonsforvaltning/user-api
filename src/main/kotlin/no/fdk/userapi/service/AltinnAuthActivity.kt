package no.fdk.userapi.service

import kotlinx.coroutines.*
import no.fdk.userapi.model.AltinnOrganization
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

val SERVICE_CODES = listOf("5977", "5755", "5756")

@Component
class AltinnAuthActivity(
    private val altinnUserService: AltinnUserService
) : CoroutineScope by CoroutineScope(Executors.newFixedThreadPool(10).asCoroutineDispatcher()) {

    fun getAuthorities(ssn: String): String {
        val resourceRoleTokens: MutableList<String> = mutableListOf()
        val authTasks = listOf(
            async { organizationAuthorities(ssn) },
            async { altinnUserService.getSysAdminAuthorities(ssn) }
        )

        runBlocking { authTasks.forEach { resourceRoleTokens.addAll(it.await()) } }

        return resourceRoleTokens.distinct().joinToString(",")
    }

    private fun allOrganizations(ssn: String): List<AltinnOrganization> {
        val orgTasks = SERVICE_CODES.map {
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

    fun getOrganizationsForTerms(ssn: String): List<AltinnOrganization> {
        val getUserTasks = SERVICE_CODES.map { async { altinnUserService.getUser(ssn, it) } }

        return runBlocking { getUserTasks.map { it.await() }.flatMap { it?.organizations ?: emptyList() } }
    }

}
