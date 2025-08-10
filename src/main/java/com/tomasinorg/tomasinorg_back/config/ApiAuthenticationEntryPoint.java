package com.tomasinorg.tomasinorg_back.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException {
        
        // Check if this is an API request
        String requestURI = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        
        if (requestURI.startsWith("/api/") || 
            (acceptHeader != null && acceptHeader.contains("application/json"))) {
            
            // Return JSON response for API requests
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"," +
                "\"loginUrl\":\"/oauth2/authorization/google\"," +
                "\"timestamp\":\"" + java.time.Instant.now() + "\"}"
            );
        } else {
            // Redirect to OAuth2 login for browser requests
            response.sendRedirect("/oauth2/authorization/google");
        }
    }
}
