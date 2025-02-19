package com.nomad.producer;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import java.util.concurrent.TimeUnit;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import lombok.extern.log4j.Log4j2;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;

@Log4j2
@Configuration
public class Neo4jConfig {

    @Value("${key_vault_uri}")
    private String key_vault_uri;
    @Value("${neo4j_uri}")
    private String neo4j_uri;
    @Value("${neo4j_user}")
    private String neo4j_user;
    @Value("${neo4j_password_key}")
    private String neo4j_password_key;

    @Bean
    public Driver neo4jDriver() {
        // Get Key Vault secrets using Azure SDK
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(key_vault_uri)
                .credential(credential)
                .buildClient();

        String password = secretClient.getSecret(neo4j_password_key).getValue();

        // Create Neo4j authentication and config
        AuthToken authToken = AuthTokens.basic(neo4j_user, password);
        Config config = Config.builder()
                .withConnectionTimeout(30, TimeUnit.SECONDS)
                .withMaxConnectionLifetime(1, TimeUnit.HOURS)
                .withMaxConnectionPoolSize(10)
                .build();

        // Return configured Neo4j driver
        return GraphDatabase.driver(neo4j_uri, authToken, config);
    }

    @Bean
    public Neo4jClient neo4jClient(Driver driver) {
        return Neo4jClient.create(driver);
    }

    @Bean
    public Session neo4jSession(Driver driver) {
        return driver.session();
    }
}