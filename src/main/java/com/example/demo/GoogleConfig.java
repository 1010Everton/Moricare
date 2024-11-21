package com.example.demo;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//@Configuration
public class GoogleConfig {

    // Obtendo valores das variáveis de ambiente configuradas no Heroku

    private String clientId;

    private String clientSecret;

    @Bean
    public GoogleAuthorizationCodeFlow authorizationCodeFlow() throws Exception {
        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                clientId,           // Variável de ambiente para client_id
                clientSecret,       // Variável de ambiente para client_secret
                List.of("https://www.googleapis.com/auth/drive")) // Escopos que você precisa
                .setAccessType("offline") // Necessário para obter o refresh token
                .build();
    }
}