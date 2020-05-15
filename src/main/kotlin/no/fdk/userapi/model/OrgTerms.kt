package no.fdk.userapi.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "org-terms")
class OrgAcceptations (
    @Id val orgnr: String,
    val acceptations: List<AcceptedTerms>
)

class AcceptedTerms (
    val acceptor: String,
    val acceptDate: LocalDate
)