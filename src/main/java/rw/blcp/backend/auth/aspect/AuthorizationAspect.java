package rw.blcp.backend.auth.aspect;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import rw.blcp.backend.auth.RoleName;
import rw.blcp.backend.auth.annotation.RequiredRoles;
import rw.blcp.backend.auth.entity.User;
import rw.blcp.backend.exception.ApiException;
import rw.blcp.backend.exception.ErrorCode;

@Slf4j
@Aspect
@Component
public class AuthorizationAspect {

  @Pointcut("@within(rw.blcp.backend.auth.annotation.RequiredRoles)")
  private void classLevelRole() {}

  @Pointcut("@annotation(rw.blcp.backend.auth.annotation.RequiredRoles)")
  private void methodLevelRole() {}

  @Pointcut("classLevelRole() || methodLevelRole()")
  private void rolesRequired() {}

  @Before("rolesRequired()")
  public void checkRoles(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();

    RequiredRoles requiredRoles = signature.getMethod().getAnnotation(RequiredRoles.class);
    if (requiredRoles == null) {
      requiredRoles = joinPoint.getTarget().getClass().getAnnotation(RequiredRoles.class);
    }
    if (requiredRoles == null) {
      return;
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof User user)) {
      log.warn("Unauthenticated access attempt on {}", joinPoint.getSignature());
      throw new ApiException(ErrorCode.TOKEN_INVALID);
    }

    Set<RoleName> userRoles =
        user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());

    boolean hasRole = Arrays.stream(requiredRoles.value()).anyMatch(userRoles::contains);
    if (!hasRole) {
      log.warn(
          "Access denied for {} — has {} but needs one of {}",
          user.getEmail(),
          userRoles,
          Arrays.asList(requiredRoles.value()));
      throw new ApiException(ErrorCode.ACCESS_DENIED);
    }
  }
}
