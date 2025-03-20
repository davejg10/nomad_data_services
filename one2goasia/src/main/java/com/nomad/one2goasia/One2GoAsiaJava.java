//package com.nomad.one2goasia;

//public class One2GoAsiaJava {

//    public One2GoAsiaScraper() {
//        // Initialize Playwright with proper configurations for scraping
//        playwright = Playwright.create();
//
//        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
//                .setHeadless(true)  // Run in headless mode for better performance
//                .setSlowMo(50));// Add delay to respect rate limits
//
//        browserContext = browser.newContext();
//        page = browserContext.newPage();
//    }
//
//    // Method to extract data from a webpage
//    public List<ScraperResponse> scrapeData(ScraperRequest request) {
//        List<ScraperResponse> scraperResponses = new ArrayList<>();
//        try {
//
//            String url = String.format("%s%s/%s?date=%s",
//                    BASE_URL,
//                    request.getSourceCity().name().toLowerCase(),
//                    request.getTargetCity().name().toLowerCase(),
//                    request.getSearchDate());
//
//            log.info("Scraping page with url: {}", url);
//            page.navigate(url);
//            page.waitForLoadState(LoadState.NETWORKIDLE);
//
////            Locator showMoreButton = page.locator(timeTableShowMoreButtonSelector);
////            int timesPressed = 0;
////            while (showMoreButton.count() > 0 && timesPressed < 3) { //only show more once for now
////                showMoreButton.scrollIntoViewIfNeeded();
////                showMoreButton.click();
////                Thread.sleep(200);
////                timesPressed ++;
////                showMoreButton = page.locator(timeTableShowMoreButtonSelector);
////            }
//            page.setDefaultTimeout(10000);
//            page.waitForSelector(tripListCard);
//            List<Locator> divList = page.locator(tripListCard).all();
//
//            List<RouteDTO> routes = new ArrayList<>();
//            for (Locator divListRow : divList) {
//                try {
//                    routes.add(parseDivListRow(divListRow, request.getSearchDate()));
//                } catch (Exception e) {
//                    log.error("There was an exception trying to parse the following row: {}", divListRow.innerHTML());
//                    log.error("Exception was: {}", e.getMessage(), e);
//                }
//            }
//
//            log.info(routes);
//
//
//        } catch (PlaywrightException e) {
//            log.error("Playwright Scraping error: " + e.getMessage());
//        }
//        return scraperResponses;
//    }
//
//    private RouteDTO parseDivListRow(Locator divListRow, LocalDate searchDate) throws Exception {
//        Map<String, String> trip = new HashMap<>();
//
//        Locator anchor = divListRow.locator("a").first();
//        String href = anchor.getAttribute("href");
//        trip.put("href", href);
//
//        List<Locator> items = anchor.locator("div.trip-body div.points div.item").all();
//        for (Locator item : items) {
//            if (item.innerHTML().contains("vehclasses")) {
//                Locator vehClasses = item.locator("div.vehclasses > span");
//                String transport = vehClasses.getAttribute("tooltip");
//                String[] transportSplit = transport.split(" with ", 2);
//                trip.put("transportType", transportSplit[0]);
//                trip.put("transportOperator", transportSplit[1]);
//            } else {
//                if (item.innerHTML().contains("trip-time dep")) {
//                    Locator departTime = item.locator("strong.time");
//                    if (departTime.equals("--:--")) {
//                        throw new ScrapingDataSchemaException("Depart time is --:--. Rejecting.");
//                    }
//                    trip.put("departure", departTime.innerText());
//                    Locator departLocation = item.locator("div.one-line");
//                    trip.put("departureLocation", departLocation.innerText());
//                } else {
//                    Locator departTime = item.locator("strong.time");
//                    trip.put("arrival", departTime.innerText());
//                    Locator departLocation = item.locator("div.one-line");
//                    trip.put("arrivalLocation", departLocation.innerText());
//                }
//            }
//        }
//
//        // 3. Get the trip-cta content
//        String priceLocator = anchor.locator("div.trip-cta > div.price > meta[data-qa='trip-card-meta-price']").getAttribute("content");
//
//        trip.put("cost", priceLocator);
//
//        RouteDTO newRoute = RouteDTO.createWithSchema(
//                trip.get("transportType"),
//                trip.get("transportOperator"),
//                trip.get("departure"),
//                trip.get("arrival"),
//                trip.get("cost"),
//                trip.get("href"),
//                searchDate
//        );
//
//        return newRoute;
//}
