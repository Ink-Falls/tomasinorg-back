package com.tomasinorg.tomasinorg_back.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleCalendarConfig {
    
    private static final String APPLICATION_NAME = "TomasinOrg Calendar Integration";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    @Bean
    public NetHttpTransport httpTransport() throws Exception {
        return GoogleNetHttpTransport.newTrustedTransport();
    }
    
    @Bean
    public JsonFactory jsonFactory() {
        return JSON_FACTORY;
    }
    
    @Bean
    public Calendar.Builder calendarBuilder(NetHttpTransport httpTransport, JsonFactory jsonFactory) {
        return new Calendar.Builder(httpTransport, jsonFactory, null)
                .setApplicationName(APPLICATION_NAME);
    }
}
