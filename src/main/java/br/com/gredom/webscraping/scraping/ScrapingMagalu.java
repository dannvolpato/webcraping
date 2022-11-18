package br.com.gredom.webscraping.scraping;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@RestController
@RequiredArgsConstructor
public class ScrapingMagalu {

    private static final Company company = Company.MAGALU;
    private final WebClient webClient;

    @GetMapping
    public List<ScrapingResult> execute() throws Exception {

        List<ScrapingResult> result = new ArrayList<>();

        String baseUrl = "https://www.magazineluiza.com.br/";
        HtmlPage page = webClient.getPage(baseUrl);

        List<HtmlAnchor> departamentos = page.getByXPath("//*[@id=\"__next\"]/div/main/section[1]/div[2]/header/div/div[3]/nav/ul/li[1]/div[2]/div/div/div[1]/ul/li[*]/a");

        var itens = departamentos.stream()
                .collect(Collectors.toMap(e -> e.getHrefAttribute(), e -> e.asNormalizedText()));

        List<Future<List<ScrapingResult>>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (var item : itens.entrySet()) {

            String link = item.getKey();
            String deptName = item.getValue();

//            result.addAll(executeDept(link, deptName));

            Future<List<ScrapingResult>> future = executor.submit(() -> executeDept(link, deptName));
            futures.add(future);
        }

        executor.shutdown();

        futures.forEach(f -> {
            try {
                result.addAll(f.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        System.out.println(String.format("Result: %s", result.size()));

        return (result);
    }

    private List<ScrapingResult> executeDept(String link, String deptName) {

        List<ScrapingResult> result = new ArrayList<>();
        HtmlPage page;

        int numberPage = 1;
        boolean goToNextPage = true;

        while (goToNextPage) {
            String linkPaginado = String.format("%s%s%s", link, "?page=", numberPage);
            System.out.println(linkPaginado);
            try {
                page = webClient.getPage(linkPaginado);

                List<HtmlDivision> produtos = page.getByXPath("//div[@data-testid=\"product-card-content\"]");

                for (var produto : produtos) {
                    HtmlHeading2 h2 = produto.getFirstByXPath("./h2");
                    var productName = h2.asNormalizedText();

                    HtmlParagraph p = produto.getFirstByXPath(".//p[@data-testid=\"price-value\"]");

                    var descricaoPreco = p.asNormalizedText();

                    result.add(ScrapingResult.build(company, deptName, productName, parsePrice(descricaoPreco)));
                }

                goToNextPage = !CollectionUtils.isEmpty(produtos);

            } catch (Exception e) {
                goToNextPage = false;
            }
            numberPage++;
        }

        return result;
    }

    private BigDecimal parsePrice(String priceAsString) {
        priceAsString = priceAsString.replaceAll("[^0-9,]", "")
                .replaceAll(",", ".");
        return new BigDecimal(priceAsString);
    }
}