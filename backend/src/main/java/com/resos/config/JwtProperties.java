package com.resos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "resos.jwt")
@Getter
@Setter
public class JwtProperties {

    private String issuer = "resos-api";
    private Duration accessTokenExpiration = Duration.ofMinutes(15);
    private Duration refreshTokenExpiration = Duration.ofDays(7);
    private String refreshCookieName = "refreshToken";
    private String refreshCookiePath = "/api/v1/auth";
    private String privateKey;
    private String publicKey;
    private boolean generateKeys = false;
}
