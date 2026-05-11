package rw.blcp.backend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.blcp.backend.core.auth.filter.JwtAuthFilter;
import rw.blcp.backend.core.auth.repository.UserRepository;
import rw.blcp.backend.core.auth.repository.UserSessionRepository;
import rw.blcp.backend.core.auth.service.JwtService;

@EnableAsync
@Configuration
public class AppConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserRepository userRepository, UserSessionRepository userSessionRepository) {
    return new JwtAuthFilter(jwtService, userRepository, userSessionRepository);
  }

  @Bean
  public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
    FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }
}
