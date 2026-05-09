package rw.blcp.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import rw.blcp.backend.auth.filter.JwtAuthFilter;
import rw.blcp.backend.core.dto.ApiErrorResponse;
import rw.blcp.backend.exception.ErrorCode;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/v1/public/**").permitAll().anyRequest().authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                        (request, response, authException) ->
                            writeError(
                                response,
                                HttpServletResponse.SC_UNAUTHORIZED,
                                ErrorCode.TOKEN_INVALID))
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) ->
                            writeError(
                                response,
                                HttpServletResponse.SC_FORBIDDEN,
                                ErrorCode.ACCESS_DENIED)));

    return http.build();
  }

  private void writeError(HttpServletResponse response, int status, ErrorCode errorCode)
      throws java.io.IOException {
    log.warn("Security filter blocked request — {}", errorCode.name());
    response.setStatus(status);
    response.setContentType("application/json");
    ApiErrorResponse body =
        ApiErrorResponse.builder()
            .traceId(MDC.get("traceId"))
            .error(
                ApiErrorResponse.ErrorDetail.builder()
                    .errorCode(errorCode.name())
                    .errorMessage(errorCode.getMessage())
                    .build())
            .timestamp(Instant.now())
            .build();
    objectMapper.writeValue(response.getWriter(), body);
  }
}
