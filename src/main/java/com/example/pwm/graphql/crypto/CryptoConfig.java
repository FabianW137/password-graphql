package com.example.pwm.graphql.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfig {

    @Bean
    public CryptoService cryptoService(@Value("${crypto.master}") String masterB64) {
        return new CryptoService(masterB64);
    }
}
