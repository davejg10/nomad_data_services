CREATE TABLE country (
    id UUID DEFAULT (UUID()) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE city (
    id UUID DEFAULT (UUID()) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    country_id UUID REFERENCES country(id),
    CONSTRAINT unique_city_per_country UNIQUE (name, country_id)
);