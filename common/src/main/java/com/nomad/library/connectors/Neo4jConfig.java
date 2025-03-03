package com.nomad.library.connectors;

import java.util.concurrent.TimeUnit;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

public class Neo4jConfig {

    private final String azureClientId;
    private final String keyVaultUri;
    private final String neo4jUri;
    private final String neo4jUser;
    private final String neo4jPasswordKey;

    public Neo4jConfig(String azureClientId, String keyVaultUri, String neo4jUri, String neo4jUser, String neo4jPasswordKey) {
        this.azureClientId = azureClientId;
        this.keyVaultUri = keyVaultUri;
        this.neo4jUri = neo4jUri;
        this.neo4jUser = neo4jUser;
        this.neo4jPasswordKey =neo4jPasswordKey;
    }

    public Driver neo4jDriver() {
        // Get Key Vault secrets using Azure SDK
        TokenCredential credential = new ManagedIdentityCredentialBuilder().clientId(azureClientId).build();

        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(credential)
                .buildClient();

        String password = secretClient.getSecret(neo4jPasswordKey).getValue();

        // Create Neo4j authentication and config
        AuthToken authToken = AuthTokens.basic(neo4jUser, password);
        Config config = Config.builder()
                .withConnectionTimeout(30, TimeUnit.SECONDS)
                .withMaxConnectionLifetime(1, TimeUnit.HOURS)
                .withMaxConnectionPoolSize(10)
                .build();

        // Return configured Neo4j driver
        Driver driver = GraphDatabase.driver(neo4jUri, authToken, config);

        return driver;
    }
}
