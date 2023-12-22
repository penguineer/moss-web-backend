package com.penguineering.moss.wb.security.oidc;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Component
@NoArgsConstructor
@ConfigurationProperties(prefix = "auth")
public class OidcClientConfiguration {

    private String callbackBaseUrl;
    private Map<String, Client> clients = new HashMap<>();

    @Setter
    @Getter
    public static class Client {
        private String clientId;
        private String clientSecret;

    }
}