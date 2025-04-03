 CREATE TABLE IF NOT EXISTS country (
     id UUID DEFAULT (UUID()) PRIMARY KEY,
     name VARCHAR(100) NOT NULL UNIQUE,
     description TEXT
 );

 CREATE TABLE IF NOT EXISTS city (
     id UUID DEFAULT (UUID()) PRIMARY KEY,
     name VARCHAR(100) NOT NULL,
     description TEXT,
     city_metrics TEXT NOT NULL,
     country_id UUID,
     CONSTRAINT fk_country_id FOREIGN KEY (country_id) REFERENCES country(id),
     CONSTRAINT unique_city_per_country UNIQUE (name, country_id)
 );

 CREATE TABLE IF NOT EXISTS route_definition (
     id UUID DEFAULT (UUID()) PRIMARY KEY,
     popularity REAL NOT NULL,
     transport_type VARCHAR(50) NOT NULL,
     source_city_id UUID,
     target_city_id UUID,
     CONSTRAINT fk_source_city FOREIGN KEY (source_city_id) REFERENCES city(id),
     CONSTRAINT fk_target_city FOREIGN KEY (target_city_id) REFERENCES city(id),
     CONSTRAINT chk_different_cities CHECK (source_city_id <> target_city_id)
 );

 CREATE TABLE IF NOT EXISTS route_instance (
     id UUID DEFAULT (UUID()) PRIMARY KEY,
     cost NUMERIC(10,2) NOT NULL,
     search_date DATE NOT NULL,
     url TEXT NOT NULL,
     departure TIMESTAMP WITH TIME ZONE NOT NULL,
     arrival TIMESTAMP WITH TIME ZONE NOT NULL,
     operator VARCHAR(50) NOT NULL,
     departure_location VARCHAR(100) NOT NULL,
     arrival_location VARCHAR(100) NOT NULL,
     last_check TIMESTAMP WITH TIME ZONE NOT NULL,
     travel_time BIGINT,
     route_definition_id UUID,
     CONSTRAINT fk_route_definition FOREIGN KEY (route_definition_id) REFERENCES route_definition(id),
     CONSTRAINT chk_positive_amount CHECK (cost >= 0),
     CONSTRAINT chk_valid_travel_time CHECK (arrival > departure)
 );
