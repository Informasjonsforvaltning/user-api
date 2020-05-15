package no.fdk.userapi.repository

import no.fdk.userapi.model.OrgAcceptations
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface OrgTermsRepository : MongoRepository<OrgAcceptations, String>