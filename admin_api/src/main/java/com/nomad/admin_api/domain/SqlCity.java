package com.nomad.admin_api.domain;

import java.util.UUID;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "city")
@Getter
@NoArgsConstructor
public class SqlCity {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String description;

    @Column(name = "country_id")
    private UUID countryId;

    public static SqlCity of(String name, String description, UUID countryId) {
        return new SqlCity(null, name, description, countryId);
    }

    public SqlCity(UUID id, String name, String description, UUID countryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.countryId = countryId;
    }

}
