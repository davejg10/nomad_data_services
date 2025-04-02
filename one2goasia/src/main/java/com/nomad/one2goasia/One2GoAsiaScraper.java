package com.nomad.one2goasia;

import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.nomad.common_utils.domain.TransportType;
import com.nomad.scraping_library.domain.RouteDTO;
import com.nomad.scraping_library.domain.ScraperIdentifier;
import com.nomad.scraping_library.domain.ScraperRequest;
import com.nomad.scraping_library.domain.ScraperResponse;
import com.nomad.scraping_library.scraper.WebScraperInterface;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class One2GoAsiaScraper implements WebScraperInterface {

    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://12go.asia/en/travel/";
    private static final String timeTableShowMoreButtonSelector = "#best_options > div.container.block-module > div.list > button";
    private static final String tripListCard = "#best_options > div.container.block-module > div.list > div:has(a)";
    private static final int RATE_LIMIT_DELAY = 1000;

    private final Playwright playwright;
    private final Browser browser;
    private Page page;
    private final BrowserContext browserContext;

    public One2GoAsiaScraper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // Initialize Playwright with proper configurations for scraping
        playwright = Playwright.create();

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)  // Run in headless mode for better performance
                .setSlowMo(50));// Add delay to respect rate limits

        browserContext = browser.newContext();
    }

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
            page = browserContext.newPage();

            page.navigate(url);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            page.setDefaultTimeout(3000);
            // page.waitForSelector(tripListCard);

            Set<RouteDTO> routesSet = new HashSet<>();

            int timesScrolled = 0, maxScrolls = 8, lastItemCount = 0;
            while (timesScrolled < maxScrolls) {

                List<Locator> divList = page.locator(tripListCard).all();
                int currentItemCount = divList.size();
                log.info("Number of items found in div: {}", currentItemCount);

                // If no new elements were loaded, stop scrolling
                if (currentItemCount == lastItemCount) {
                    log.info("No new items found. Stopping scroll.");
                    break;
                }
                try {
                    routesSet.addAll(parseDivListRow(tripListCard, request.getSearchDate()));
                } catch (Exception e) {
                    log.error("Unexpected exception was: {}", e.getMessage(), e);
                }
                log.info("Scrolling down");
                page.mouse().wheel(0, 1200);
                Thread.sleep(200);
                timesScrolled ++;
            }

            Map<TransportType, List<RouteDTO>> groupedRoutes = routesSet.stream().collect(Collectors.groupingBy(RouteDTO::transportType));

            groupedRoutes.forEach((type, routeListForType) -> {
                ScraperResponse response = new ScraperResponse(request.getScraperRequestSource(), request.getScraperRequestType(), ScraperIdentifier.ONE2GOASIA, type, request.getSourceCity(), request.getTargetCity(), routeListForType, request.getSearchDate());
                scraperResponses.add(response);
            });

        } catch (PlaywrightException e) {
            log.error("Playwright Scraping error: " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("InterruptedException");
            throw new RuntimeException(e);
        } finally {
            log.info("Scrape complete, Closing page");
            page.close();
        }
        return scraperResponses;
    }

    private Set<RouteDTO> parseDivListRow(String tripListCard, LocalDate searchDate) throws Exception {
        Set<RouteDTO> routes = new HashSet<>();
        Object tripData = page.evalOnSelectorAll(tripListCard, """
                            elements => elements.map(el => {
                                let anchor = el.querySelector("a");
                                let href = anchor ? anchor.href : null;
                        
                                let trip = { href: href };
                        
                                let items = anchor ? anchor.querySelectorAll("div.trip-body div.points div.item") : [];
                                
                                items.forEach(item => {
                                    let innerHTML = item.innerHTML;
                        
                                    if (innerHTML.includes("vehclasses")) {
                                        let vehClasses = item.querySelector("div.vehclasses > div");
                                        let newVehClass = vehClasses ? vehClasses : item.querySelector("div.vehclasses > span");
                                        let transport = newVehClass ? newVehClass.getAttribute("tooltip") : null;
                                        if (transport) {
                                            let transportSplit = transport.split(" with ");
                                            trip.transportType = transportSplit[0] || null;
                                            trip.transportOperator = transportSplit[1] || null;
                                        }
                                    } else if (innerHTML.includes("trip-time dep")) {
                                        let departTime = item.querySelector("strong.time")?.innerText;
                                        trip.departure = departTime;
                                        trip.departureLocation = item.querySelector("div.one-line")?.innerText;
                                    } else {
                                        let arrivalTime = item.querySelector("strong.time")?.innerText;
                                        trip.arrival = arrivalTime;
                                        trip.arrivalLocation = item.querySelector("div.one-line")?.innerText;
                                    }
                                });
                                
                                let cost = anchor.querySelector("div.trip-cta > div.price > meta[data-qa='trip-card-meta-price']").getAttribute("content");
                                trip.cost = cost;
                                return trip;
                            })
                        """);
        List<Map<String, String>> tripList = objectMapper.convertValue(tripData, new TypeReference<List<Map<String, String>>>() {});

        for (Map<String, String> trip : tripList) {
            try {
                if (trip.get("departure").equals("--:--")) {
                    log.error("Not adding due to departTime being --:--");
                    continue;
                }
            } catch (NullPointerException e) {
                log.error("Nullpointer thrown when trying to trip.get(departure). Trip list map is: {}", tripList);
            }

            RouteDTO newRoute = RouteDTO.createWithSchema(
                    trip.get("transportType"),
                    trip.get("transportOperator"),
                    trip.get("departure"),
                    trip.get("arrival"),
                    trip.get("departureLocation"),
                    trip.get("arrivalLocation"),
                    trip.get("cost"),
                    trip.get("href"),
                    searchDate
            );
            routes.add(newRoute);
        }

        return routes;
    }


}
