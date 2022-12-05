package br.com.gredom.webscraping.job;

import br.com.gredom.webscraping.entity.CategoryEntity;
import br.com.gredom.webscraping.repository.CategoryRepository;
import br.com.gredom.webscraping.usecase.scraping.magalu.ScrapingMagaluHtmlunit;
import br.com.gredom.webscraping.util.Collections;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JobScrapingMagaluItems {

    private final CategoryRepository categoryRepository;
    private final ScrapingMagaluHtmlunit scrapingMagaluHtmlunit;

    public void execute() throws Exception {

        int limit = 10;
        int offset = 0;
        boolean foundCategories = true;
        String orderBy = "id";

        while (foundCategories) {

            List<CategoryEntity> categories = categoryRepository.findSelecteds(orderBy, offset, limit);
            foundCategories = Collections.nonEmpty(categories);

            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);

            for (var cat : categories) {
                scrapingMagaluHtmlunit.scrapingItems(webClient, cat.getUrl(), cat.getName());
            }

            webClient.close();

            offset++;
        }
    }
}