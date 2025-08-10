package com.tomasinorg.tomasinorg_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.mockito.Mockito;

@Configuration
@Profile("test")
public class GoogleCalendarTestConfig {

    @Bean
    public OAuth2AuthorizedClientService mockOAuth2AuthorizedClientService() {
        return Mockito.mock(OAuth2AuthorizedClientService.class);
    }
}
