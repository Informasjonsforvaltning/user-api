package no.fdk.userapi;

import no.fdk.userapi.configuration.HostProperties;
import no.fdk.userapi.configuration.SecurityProperties;
import no.fdk.userapi.configuration.WhitelistProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ HostProperties.class, SecurityProperties.class, WhitelistProperties.class })
public class UserApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApiApplication.class, args);
    }
}

