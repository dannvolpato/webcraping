package br.com.gredom.webscraping.controller;

import br.com.gredom.webscraping.response.ScrapingResponse;
import br.com.gredom.webscraping.usecase.scraping.ScrapingAmericanas;
import br.com.gredom.webscraping.usecase.scraping.ScrapingMagalu;
import br.com.gredom.webscraping.usecase.scraping.impl.ScrapingMagaluJson;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scraping")
@RequiredArgsConstructor
public class ScrapingController {

    private final ScrapingMagalu scrapingMagalu;
    private final ScrapingAmericanas scrapingAmericanas;
    private final ScrapingMagaluJson scrapingMagaluJson;

    @GetMapping("/magalu")
    public ScrapingResponse scrapingMagalu() throws Exception {
        return scrapingMagalu.execute();
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