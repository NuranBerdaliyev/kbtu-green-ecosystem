package com.example.green.config;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")/*
 talking to Spring where to take the values
 for configuration jwt parameters
 */
@Validated
public class AuthProperties {
    @NotBlank(message = "app.auth.jwt-secret must not be blank")
    @Size(min = 32, message = "app.auth.jwt-secret must be at least 32 chars")
    private String jwtSecret; //jwt key for generation access and refresh tokens

    @Min(value = 1, message = "app.auth.access-ttl-seconds must be > 0")
    private long accessTtlSeconds; //access token's existence time for getting access to defended endpoints

    @Min(value = 1, message = "app.auth.refresh-ttl-seconds must be > 0")
    private long refreshTtlSeconds; //refresh token's existence time for updating access token (for security)
    //If malicious catch access token, token can help him only for accessTtlSeconds time
}
