package com.studentreadiness.security;

import com.studentreadiness.exception.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetailsService.CustomUserPrincipal customUserPrincipal) {
            return customUserPrincipal.getId();
        }

        throw new AccessDeniedException("Unable to determine current user ID");
    }

    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetailsService.CustomUserPrincipal customUserPrincipal) {
            return customUserPrincipal.getUsername();
        }

        return auth.getName();
    }
}
