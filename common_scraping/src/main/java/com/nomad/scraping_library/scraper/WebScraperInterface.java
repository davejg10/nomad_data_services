package com.nomad.scraping_library.scraper;

import java.util.List;

import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperResponse;

public interface WebScraperInterface {

    public List<ScraperResponse> scrapeData(ScraperRequest request);
    
}
