package com.nomad.library.messages;

import com.nomad.library.domain.Neo4jCity;

public record ScraperJob(String scraperJobSource, ScraperJobType type, Neo4jCity sourceCity, Neo4jCity destinationCity, String searchDate) {}