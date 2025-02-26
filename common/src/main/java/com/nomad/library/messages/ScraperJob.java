package com.nomad.library.messages;

import com.nomad.library.domain.City;

public record ScraperJob(String scraperJobSource, ScraperJobType type, City sourceCity, City destinationCity, String searchDate) {}