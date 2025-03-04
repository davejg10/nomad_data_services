// package com.nomad.admin_api;

// import org.neo4j.driver.Driver;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
// import org.springframework.data.transaction.ChainedTransactionManager;
// import org.springframework.orm.jpa.JpaTransactionManager;
// import org.springframework.transaction.PlatformTransactionManager;
// import org.springframework.transaction.annotation.EnableTransactionManagement;

// import jakarta.persistence.EntityManagerFactory;

// @Configuration
// @EnableTransactionManagement
// public class TransactionConfig {
    
//     @Bean
//     public PlatformTransactionManager postgresTransactionManager(EntityManagerFactory emf) {
//         return new JpaTransactionManager(emf);
//     }

//     @Bean
//     public PlatformTransactionManager neo4jTransactionManager(Driver driver) {
//         return new Neo4jTransactionManager(driver);
//     }

//     // Combine PostgreSQL & Neo4j into a single transaction
//     @Bean
//     public ChainedTransactionManager transactionManager(
//             PlatformTransactionManager postgresTransactionManager,
//             PlatformTransactionManager neo4jTransactionManager) {
//         return new ChainedTransactionManager(postgresTransactionManager, neo4jTransactionManager);
//     }
// }
