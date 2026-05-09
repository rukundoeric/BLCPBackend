package rw.blcp.backend.auth.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.auth.repository.UserRepository;
import rw.blcp.backend.auth.service.JwtService;
import rw.blcp.backend.core.RecordState;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.validateAndExtract(token);
            UUID userId = UUID.fromString(claims.getSubject());

            User user =
                    userRepository
                            .findById(userId)
                            .orElseThrow(
                                    () -> new ApiException(rw.blcp.backend.exception.ErrorCode.TOKEN_INVALID));

            if (user.getState() != RecordState.ACTIVE) {
                filterChain.doFilter(request, response);
                return;
            }

            var authorities =
                    user.getRoles().stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                            .toList();

            var auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (ApiException e) {
            if (e.getErrorCode() == ErrorCode.TOKEN_EXPIRED) {
                log.debug("Expired token on {}", request.getRequestURI());
            } else {
                log.warn("Invalid token on {}: {}", request.getRequestURI(), e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
