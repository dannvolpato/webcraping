package br.com.gredom.webscraping.controller;

import br.com.gredom.webscraping.job.JobScrapingMagaluItems;
import br.com.gredom.webscraping.response.ScrapingResponse;
import br.com.gredom.webscraping.usecase.scraping.americanas.ScrapingAmericanas;
import br.com.gredom.webscraping.usecase.scraping.magalu.ScrapingMagaluHtmlunit;
import br.com.gredom.webscraping.usecase.scraping.magalu.ScrapingMagaluJson;
import br.com.gredom.webscraping.usecase.scraping.magalu.ScrapingMagaluSelenium;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scraping")
@RequiredArgsConstructor
public class ScrapingController {

    private final ScrapingMagaluHtmlunit scrapingMagaluHtmlunit;
    private final ScrapingAmericanas scrapingAmericanas;
    private final ScrapingMagaluJson scrapingMagaluJson;
    private final ScrapingMagaluSelenium scrapingMagaluSelenium;
    private final JobScrapingMagaluItems jobScrapingMagaluItems;

    @GetMapping("/magalu/h")
    public ScrapingResponse scrapingMagaluHtmlunit() throws Exception {
        return scrapingMagaluHtmlunit.execute();
    }

    @GetMapping("/magalu/h/job")
    public void jobMagalu() throws Exception {
        jobScrapingMagaluItems.execute();
    }

    @GetMapping("/magalu/s")
    public ScrapingResponse scrapingMagaluSelenium() throws Exception {
        return scrapingMagaluSelenium.execute();
    }

    @GetMapping("/magalu/json")
    public ScrapingResponse scrapingMagaluJson() throws Exception {
        return scrapingMagaluJson.execute();
    }

    @GetMapping("/americanas")
    public ScrapingResponse scrapingAmericanas() throws Exception {
        return scrapingAmericanas.execute();
    }
}