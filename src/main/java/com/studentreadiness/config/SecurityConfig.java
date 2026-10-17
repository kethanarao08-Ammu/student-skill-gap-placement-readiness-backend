
package com.studentreadiness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentreadiness.security.CustomUserDetailsService;
import com.studentreadiness.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String DEPLOYED_FRONTEND_ORIGIN =
            "https://student-skill-gap-placement-readine.vercel.app";

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider tokenProvider;

    @Value("${cors.allowed-origins:}")
    private String allowedOrigins;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtTokenProvider tokenProvider) {
        this.userDetailsService = userDetailsService;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Stream.concat(
                        Arrays.stream(allowedOrigins.split(",")),
                        Stream.of(DEPLOYED_FRONTEND_ORIGIN))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .distinct()
                .toList();

        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
        ));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(cors ->
                    cors.configurationSource(corsConfigurationSource()))

            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Allow browser preflight requests.
                .requestMatchers(
                        org.springframework.http.HttpMethod.OPTIONS,
                        "/**"
                ).permitAll()

                // Registration and login endpoints are public.
                .requestMatchers("/auth/**").permitAll()

                // Admin endpoints require the admin role.
                .requestMatchers("/admin/**")
                        .hasAuthority("ROLE_ADMIN")

                // Other endpoints require authentication.
                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(
                    (request, response, authException) -> {
                        response.setStatus(
                                HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(
                                MediaType.APPLICATION_JSON_VALUE);

                        ObjectMapper mapper = new ObjectMapper();

                        mapper.writeValue(
                            response.getWriter(),
                            Map.of(
                                "status", 401,
                                "message",
                                "Unauthorized: Authentication token is required or invalid."
                            )
                        );
                    }
                )

                .accessDeniedHandler(
                    (request, response, accessDeniedException) -> {
                        response.setStatus(
                                HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType(
                                MediaType.APPLICATION_JSON_VALUE);

                        ObjectMapper mapper = new ObjectMapper();

                        mapper.writeValue(
                            response.getWriter(),
                            Map.of(
                                "status", 403,
                                "message",
                                "Forbidden: You do not have permission to access this resource."
                            )
                        );
                    }
                )
            )

            .authenticationProvider(authenticationProvider())

            .addFilterBefore(
                    jwtAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
