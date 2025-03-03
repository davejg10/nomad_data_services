// package com.nomad.admin_api;

// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;

// import org.springframework.stereotype.Repository;

// import com.nomad.admin_api.domain.CountryDTO;
// import com.nomad.library.domain.Country;

// import lombok.extern.log4j.Log4j2;

// @Repository
// @Log4j2
// public class PostgresRepository {

//     private final Connection postgresClient;

//     public PostgresRepository(Connection postgresClient) {
//         this.postgresClient = postgresClient;
//     }

//     public Country createCountry(CountryDTO countryDTO) throws Exception {

//         PreparedStatement st = postgresClient.prepareStatement("""
//             WITH inserted AS (
//                 INSERT INTO country (name, description) 
//                 SELECT ?, ?
//                 WHERE NOT EXISTS (
//                     SELECT 1 FROM country WHERE name = ?
//                 )
//                 RETURNING id, name
//             )
//             SELECT id, name FROM inserted
//             UNION
//             SELECT id, name FROM country WHERE name = ?;
//         """);
//         st.setString(1, countryDTO.name());
//         st.setString(2, countryDTO.description());
//         st.setString(3, countryDTO.name());
//         st.setString(4, countryDTO.name());


//         ResultSet rs = st.executeQuery();
//         rs.next();
//         String uuid = rs.getString(1);
//         log.info("Country %s inserted", rs.getString(2));

//         return new Country(uuid, countryDTO.name());            
//     }
// }


