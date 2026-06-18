package com.resos.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
@Getter
public class JwtKeyProvider {

    private final JwtProperties jwtProperties;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws GeneralSecurityException {
        if (hasConfiguredKeys()) {
            privateKey = loadPrivateKey(jwtProperties.getPrivateKey());
            publicKey = loadPublicKey(jwtProperties.getPublicKey());
            return;
        }

        if (jwtProperties.isGenerateKeys()) {
            KeyPair keyPair = generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
            log.warn("Generated ephemeral RSA keys for JWT signing (dev/test only)");
            return;
        }

        throw new IllegalStateException(
                "JWT keys not configured. Set resos.jwt.private-key/public-key or resos.jwt.generate-keys=true");
    }

    private boolean hasConfiguredKeys() {
        return jwtProperties.getPrivateKey() != null && !jwtProperties.getPrivateKey().isBlank()
                && jwtProperties.getPublicKey() != null && !jwtProperties.getPublicKey().isBlank();
    }

    private KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private PrivateKey loadPrivateKey(String pem) throws GeneralSecurityException {
        String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private PublicKey loadPublicKey(String pem) throws GeneralSecurityException {
        String normalized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }
}
