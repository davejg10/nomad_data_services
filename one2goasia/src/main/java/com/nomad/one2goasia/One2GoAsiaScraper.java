package com.nomad.one2goasia;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.nomad.scraping_library.domain.TransportType;
import com.nomad.scraping_library.domain.RouteDTO;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperResponse;
import com.nomad.scraping_library.scraper.WebScraperInterface;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Log4j2
public class One2GoAsiaScraper implements WebScraperInterface {

    private static final String BASE_URL = "https://12go.asia/en/travel/";
    private static final String timeTableShowMoreButtonSelector = "#__nuxt > div.wrapper > div.wrapper-content > div.page-search-results > div:nth-child(6) > div > button";
    private static final String timeTableListSelector =  "#__nuxt > div.wrapper > div.wrapper-content > div.page-search-results > div:nth-child(6) > div > div > table > tbody";
    private static final int RATE_LIMIT_DELAY = 1000;

    private final Playwright playwright;
    private final Browser browser;
    private final Page page;
    private final BrowserContext browserContext;
   

    public One2GoAsiaScraper() {
        // Initialize Playwright with proper configurations for scraping
        playwright = Playwright.create();

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)  // Run in headless mode for better performance
                .setSlowMo(50));    // Add delay to respect rate limits

        browserContext = browser.newContext();
        page = browserContext.newPage();
    }

    // @PreDestroy
    // public void onDestroy() throws InterruptedException {
    //     log.info("Closing playwright gracefully...");
    //     page.close();
    //     Thread.sleep(2000);

    //     browserContext.close();
    //     Thread.sleep(2000);

    //     browser.close();
    //     Thread.sleep(2000);
    //     playwright.close();

    //     Thread.sleep(1000);

    // }

    // Method to extract data from a webpage
    public List<ScraperResponse> scrapeData(ScraperRequest request) {
        List<ScraperResponse> scraperResponses = new ArrayList<>();
        try {

            String url = String.format("%s%s/%s?date=%s",
                    BASE_URL,
                    request.getSourceCity().name().toLowerCase(),
                    request.getTargetCity().name().toLowerCase(),
                    request.getSearchDate());

            log.info("Scraping page with url: {}", url);
            page.navigate(url);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            Locator showMoreButton = page.locator(timeTableShowMoreButtonSelector);
            while (showMoreButton.count() > 0) {
                showMoreButton.scrollIntoViewIfNeeded();
                showMoreButton.click();
                Thread.sleep(400);
                showMoreButton = page.locator(timeTableShowMoreButtonSelector);
            }
            Locator tableBody = page.locator(timeTableListSelector);
            tableBody.waitFor();

            List<RouteDTO> results = parseTableBody(tableBody.innerText(), request.getSearchDate());
            Thread.sleep(500);
            for (RouteDTO routeDTO  : results) {
                ScraperResponse response = new ScraperResponse(request.getScraperRequestSource(), request.getType(), request.getSourceCity(), request.getTargetCity(), routeDTO, request.getSearchDate());
                scraperResponses.add(response);
            }

        } catch (PlaywrightException e) {
            log.error("Playwright Scraping error: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return scraperResponses;
    }

    private List<RouteDTO> parseTableBody(String tableBodyInnerText, LocalDate searchDate) {
        // Split on line breaks
        String[] lines = tableBodyInnerText.split("\\R+");
        // Remove empty entries
        List<String> lineList = Arrays.stream(lines).map(String::trim).filter(line -> !line.equals("")).toList();

        List<String> tempLine = new ArrayList<>();
        List<RouteDTO> routes = new ArrayList<>();

        for(int index = 0; index < lineList.size(); index++) {
            String line = lineList.get(index);
            if (index != 0 && TransportType.to12GoAsiaList().contains(line)) {
                RouteDTO route;
                try {
                    if (tempLine.get(3).contains("Any time")) {
                        log.warn("Ignoring route as contains Any Time for depart");
                        tempLine.clear();
                        tempLine.add(line);
                        continue;
                    }
                    String transportType = tempLine.get(0);
                    String operator = tempLine.get(1);
                    String depart = tempLine.get(3);
                    String arrive = tempLine.get(4);
                    String cost = tempLine.get(5);
                    log.info("transportType: {}, operator: {}, depart: {}, arrive: {}, cost: {}", transportType, operator, depart, arrive, cost);
                    route = RouteDTO.createWithSchema(
                            transportType,
                            operator,
                            depart,
                            arrive,
                            cost.length() <= 6 ? Double.parseDouble(cost.substring(1)) : 1000.0,
                            searchDate
                    );
                    routes.add(route);
                } catch (Exception e) {
                    log.error("Issue when trying to map scraped data to RouteDTO object. Error: {}", e.getMessage());
                }
                tempLine.clear();
            }
            tempLine.add(line);
        }
        return routes;
    }

}
