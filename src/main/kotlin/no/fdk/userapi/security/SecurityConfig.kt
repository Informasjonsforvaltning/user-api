package no.fdk.userapi.security

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
open class SecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()

        http.cors()
            .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/terms/**")
                        .authenticated()
                    .antMatchers(HttpMethod.GET)
                        .permitAll()
                    .anyRequest()
                        .authenticated()
            .and()
                .oauth2ResourceServer()
                    .jwt()
    }

    @Bean
    open fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder? {
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(properties.jwt.jwkSetUri).build()
        jwtDecoder.setJwtValidator(DelegatingOAuth2TokenValidator(AudienceValidator()))
        return jwtDecoder
    }
}
