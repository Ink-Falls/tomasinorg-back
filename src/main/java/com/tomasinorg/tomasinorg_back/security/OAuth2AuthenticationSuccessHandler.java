package com.tomasinorg.tomasinorg_back.security;

import com.tomasinorg.tomasinorg_back.model.Role;
import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String googleId = (String) attributes.get("sub");

        // Find user (should already be created/updated by CustomOAuth2UserService)
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> createNewUser(email, name, picture, googleId));

        // Generate JWT tokens
        String jwtAccessToken = tokenProvider.generateToken(email, user.getRole().toString());
        String jwtRefreshToken = tokenProvider.generateRefreshToken(email);

        // Update user's JWT refresh token in database (keep Google tokens separate)
        user.setRefreshToken(jwtRefreshToken);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Set cookies
        setTokenCookie(response, "accessToken", jwtAccessToken, (int) (tokenProvider.getJwtExpirationMs() / 1000));
        setTokenCookie(response, "refreshToken", jwtRefreshToken, (int) (tokenProvider.getJwtRefreshExpirationMs() / 1000));

        // Redirect to frontend
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/oauth2/redirect");
    }

    private User createNewUser(String email, String name, String picture, String googleId) {
        // Hardcoded admin email check
        Role role = "lennardacef@gmail.com".equals(email) ? Role.ADMIN : Role.USER;
        
        return userRepository.save(User.builder()
                .email(email)
                .name(name)
                .picture(picture)
                .googleId(googleId)
                .role(role)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private void setTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        // Note: SameSite=Strict and Secure flags would be set in production
        response.addCookie(cookie);
    }
}
