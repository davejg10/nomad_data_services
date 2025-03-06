package com.nomad.library.scraper;

import java.util.List;

import com.nomad.library.messages.ScraperRequest;
import com.nomad.library.messages.ScraperResponse;

public interface WebScraperInterface {

    public List<ScraperResponse> scrapeData(ScraperRequest request);
    
}
