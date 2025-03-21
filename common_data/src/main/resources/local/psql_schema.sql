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
    country_id UUID REFERENCES country(id),
    CONSTRAINT unique_city_per_country UNIQUE (name, country_id)
);