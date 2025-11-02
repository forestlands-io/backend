package io.forestlands.backend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            String scope = jwt.getClaimAsString("scope");
            if (scope == null || scope.isBlank()) return List.of();
            return (Collection<GrantedAuthority>) scopeToAuthorities(scope);
        });
        return conv;
    }

    private Collection<? extends GrantedAuthority> scopeToAuthorities(String scope) {
        return scope.lines()
                .flatMap(s -> List.of(s.split("\\s+")).stream())
                .filter(s -> !s.isBlank())
                .map(s -> (GrantedAuthority) () -> "SCOPE_" + s)
                .toList();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
    }

    @Bean
    public JwtEncoder jwtEncoder(@Value("${jwt.secret}") String base64UrlSecret) {
        byte[] secret = java.util.Base64.getUrlDecoder().decode(base64UrlSecret);
        return new NimbusJwtEncoder(new ImmutableSecret<>(secret));
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.secret}") String base64UrlSecret) {
        byte[] secret = java.util.Base64.getUrlDecoder().decode(base64UrlSecret);
        SecretKeySpec key = new SecretKeySpec(secret, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
