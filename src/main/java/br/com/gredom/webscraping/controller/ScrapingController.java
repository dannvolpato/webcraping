package br.com.gredom.webscraping.controller;

import br.com.gredom.webscraping.usecase.scraping.ScrapingAmericanas;
import br.com.gredom.webscraping.usecase.scraping.ScrapingMagalu;
import br.com.gredom.webscraping.response.ScrapingResponse;
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

    @GetMapping("/magalu")
    public ScrapingResponse scrapingMagalu() throws Exception {
        return scrapingMagalu.execute();
    }

    @GetMapping("/americanas")
    public ScrapingResponse scrapingAmericanas() throws Exception {
        return scrapingAmericanas.execute();
    }
}