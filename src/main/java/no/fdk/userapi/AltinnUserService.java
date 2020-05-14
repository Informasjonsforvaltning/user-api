package no.fdk.userapi;

import no.fdk.userapi.adapter.AltinnAdapter;
import no.fdk.userapi.configuration.WhitelistProperties;
import no.fdk.userapi.model.AltinnOrganization;
import no.fdk.userapi.model.AltinnPerson;
import no.fdk.userapi.model.RoleFDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static no.fdk.userapi.model.RoleFDK.ResourceType.Organization;
import static no.fdk.userapi.model.RoleFDK.Role.Admin;

@Service
public class AltinnUserService {
    @Autowired
    private AltinnAdapter altinnAdapter;
    @Autowired
    private WhitelistProperties whitelists;

    private final Predicate<AltinnOrganization> organizationFilter = (o) -> whitelists.getOrgNrWhitelist().contains(o.getOrganizationNumber()) || whitelists.getOrgFormWhitelist().contains(o.getOrganizationForm());

    AltinnUserService(WhitelistProperties whitelists, AltinnAdapter altinnAdapter) {
        this.altinnAdapter = altinnAdapter;
        this.whitelists = whitelists;
    }

    Optional<User> getUser(String id) {
        // Currently we only fetch one role association from Altinn
        // and we interpret it as publisher admin role in fdk system

        return altinnAdapter.getPerson(id).map(AltinnUserService.AltinnUserAdapter::new);
    }

    Optional<String> getAuthorities(String id) {
        return altinnAdapter.getPerson(id).map(this::getPersonAuthorities);
    }

    private String getPersonAuthorities(AltinnPerson person) {

        List<String> resourceRoleTokens = person.getOrganizations().stream()
            .filter(organizationFilter)
            .map(o -> new RoleFDK(Organization, o.getOrganizationNumber(), Admin))
            .map(Object::toString)
            .collect(Collectors.toList());

        if (whitelists.getAdminList().contains(person.getSocialSecurityNumber())) {
            resourceRoleTokens.add(RoleFDK.Companion.getROOT_ADMIN().toString());
        }

        return String.join(",", resourceRoleTokens);
    }

    private static class AltinnUserAdapter implements User {
        private AltinnPerson person;

        AltinnUserAdapter(AltinnPerson person) {
            this.person = person;
        }

        public String getId() {
            return person.getSocialSecurityNumber();
        }

        private List<String> getNames() {
            return Arrays.asList(person.getName().split("\\s+"));
        }

        public String getFirstName() {
            List<String> firstNamesList = getNames().subList(0, (getNames().size() - 1));
            return String.join(" ", firstNamesList);
        }

        public String getLastName() {
            return getNames().get(getNames().size() - 1);
        }
    }
}
