package no.fdk.userapi.mapper

import no.fdk.userapi.model.RoleFDK

private const val ADMIN = "ead5e6c2-95ad-4dc7-8172-3ecdb1f0e127"
private const val WRITE = "2036c732-f82f-4f40-8b3d-9b678dae6c07"
private const val READ = "1f8c1441-dab0-43a5-8984-4e9f12f4e2da"

fun mapAuthoritiesFromOK(roles: List<String>, orgNames: List<String>): String {
    var orgIndex = 0
    val fdkRoles = roles.mapNotNull {
        if (orgIndex < orgNames.size) {
            val fdkRole = when(it) {
                ADMIN ->  {
                    orgNameToNumber(orgNames[orgIndex])?.let { orgId ->
                        RoleFDK(RoleFDK.ResourceType.Organization, orgId, RoleFDK.Role.Admin)
                    }
                }
                WRITE -> {
                    orgNameToNumber(orgNames[orgIndex])?.let { orgId ->
                        RoleFDK(RoleFDK.ResourceType.Organization, orgId, RoleFDK.Role.Write)
                    }
                }
                READ ->  {
                    orgNameToNumber(orgNames[orgIndex])?.let { orgId ->
                        RoleFDK(RoleFDK.ResourceType.Organization, orgId, RoleFDK.Role.Read)
                    }
                }
                else -> null
            }
            orgIndex++
            fdkRole
        } else null
    }

    return fdkRoles.joinToString(",")
}

fun orgNameToNumber(name: String): String? =
    when (name) {
        "Barne- og familieetaten" -> "976819896"
        "Beredskapsetaten" -> "976820096"
        "Boligbygg Oslo KF" -> "974780747"
        "Brann- og redningsetaten" -> "876820102"
        "Byantikvaren" -> "976819993"
        "Bydel Alna" -> "970534644"
        "Bydel Bjerke" -> "974778874"
        "Bydel Frogner" -> "874778702"
        "Bydel Gamle Oslo" -> "974778742"
        "Bydel Grorud" -> "974778866"
        "Bydel Grünerløkka" -> "870534612"
        "Bydel Nordre Aker" -> "974778882"
        "Bydel Nordstrand" -> "970534679"
        "Bydel Sagene" -> "974778726"
        "Bydel St. Hanshaugen" -> "971179686"
        "Bydel Stovner" -> "874778842"
        "Bydel Søndre Nordstrand" -> "972408875"
        "Bydel Ullern" -> "971022051"
        "Bydel Vestre Aker" -> "970145311"
        "Bydel Østensjø" -> "974778807"
        "Bymiljøetaten" -> "996922766"
        "Byrådsavdelingene" -> null
        "Bystyrets sekretariat" -> "976819853"
        "Deichman bibliotek" -> "919770775"
        "Eiendoms- og byfornyelsesetaten" -> "874780782"
        "Fornebubanen" -> "818379862"
        "Gravferdsetaten" -> "976820010"
        "Helseetaten" -> "997506499"
        "Innkrevingsetaten" -> "976819934"
        "Klimaetaten" -> "876819902"
        "Kommuneadvokaten" -> "976819942"
        "Kommunerevisjonen" -> "976819861"
        "Kulturetaten" -> "992410213"
        "Munchmuseet" -> "995138670"
        "Næringsetaten" -> "979594887"
        "Origo - digitaliseringsenhet" -> "920204368"
        "Oslo Havn KF" -> "987592567"
        "Oslobygg KF" -> "924599545"
        "Pasient- og brukerombudet" -> "984372140"
        "Plan- og bygningsetaten" -> "971040823"
        "Renovasjons- og gjenvinningsetaten" -> "923954791"
        "Rådhusets forvaltningstjeneste" -> "976819918"
        "Sykehjemsetaten" -> "990612498"
        "Utdanningsetaten" -> "976820037"
        "Utviklings- og kompetanseetaten" -> "971183675"
        "Drift" -> "971183675"
        "Vann- og avløpsetaten" -> "971185589"
        "Velferdsetaten" -> "997506413"
        else -> null
}
