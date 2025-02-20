package com.nomad.consumer;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.nomad.consumer.nomad.RouteInfo;
import com.nomad.consumer.nomad.TransportType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class WebScraper {

   private static final String BASE_URL = "https://12go.asia/en/travel/";
   private static final String timeTableShowMoreButtonSelector = "#__nuxt > div.wrapper > div.wrapper-content > div.page-search-results > div:nth-child(6) > div > button";
   private static final String timeTableListSelector =  "#__nuxt > div.wrapper > div.wrapper-content > div.page-search-results > div:nth-child(6) > div > div > table > tbody";
   private static final int RATE_LIMIT_DELAY = 1000;

   private final Playwright playwright;
   private final Browser browser;
   private final Page page;

   public WebScraper() {
       // Initialize Playwright with proper configurations for scraping
       playwright = Playwright.create();
       browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
               .setHeadless(true)  // Run in headless mode for better performance
               .setSlowMo(50));    // Add delay to respect rate limits
       page = browser.newContext().newPage();
   }


   // Method to extract data from a webpage
   public List<RouteInfo> scrapeData(String sourceName, String destinationName, String date) {
       List<RouteInfo> results = new ArrayList<>();
       try {
           String url = String.format("%s%s/%s",
                   BASE_URL,
                   sourceName.toLowerCase(),
                   destinationName.toLowerCase(),
                   date);

           log.info("Scraping page with url: {}", url);
           page.navigate(url);
           page.waitForLoadState(LoadState.NETWORKIDLE);

           Locator showMoreButton = page.locator(timeTableShowMoreButtonSelector);
           while (showMoreButton.count() > 0) {
               showMoreButton.scrollIntoViewIfNeeded();
               showMoreButton.click();
               Thread.sleep(RATE_LIMIT_DELAY);
               showMoreButton = page.locator(timeTableShowMoreButtonSelector);
           }

           Locator tableBody = page.locator(timeTableListSelector);
           tableBody.waitFor();

           results = parseTableBody(tableBody.innerText());
           Thread.sleep(RATE_LIMIT_DELAY);

       } catch (PlaywrightException e) {
           System.err.println("Scraping error: " + e.getMessage());
       } catch (InterruptedException e) {
           throw new RuntimeException(e);
       }
       return results;
   }

   private List<RouteInfo> parseTableBody(String tableBodyInnerText) {
       // Split on line breaks
       String[] lines = tableBodyInnerText.split("\\R+");
       // Remove empty entries
       List<String> lineList = Arrays.stream(lines).map(String::trim).filter(line -> !line.equals("")).toList();

       List<String> tempLine = new ArrayList<>();
       List<RouteInfo> routes = new ArrayList<>();

       for(int index = 0; index < lineList.size(); index++) {
           String line = lineList.get(index);
           if (index != 0 && TransportType.to12GoAsiaList().contains(line)) {
               RouteInfo route = new RouteInfo(
                       TransportType.valueOf(tempLine.get(0).toUpperCase()),
                       tempLine.get(1),
                       tempLine.get(3),
                       tempLine.get(4),
                       tempLine.get(5).length() <= 6 ? Double.parseDouble(tempLine.get(5).substring(1)) : 1000.0
               );
               tempLine.clear();
               routes.add(route);
           }
           tempLine.add(line);
       }
       return routes;
   }

}
