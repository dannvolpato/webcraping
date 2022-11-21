package br.com.gredom.webscraping.usecase.scraping;

import br.com.gredom.webscraping.dto.ScrapingItemDto;
import br.com.gredom.webscraping.enums.Company;
import br.com.gredom.webscraping.response.ScrapingResponse;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapingMagalu {

    private static final Company company = Company.MAGALU;
    private final WebClient webClient;

    public ScrapingResponse execute() throws Exception {

        ScrapingResponse response = ScrapingResponse.build();

        String baseUrl = "https://www.magazineluiza.com.br/";
        HtmlPage page = webClient.getPage(baseUrl);

        List<HtmlAnchor> departamentos = page.getByXPath("//*[@id=\"__next\"]/div/main/section[1]/div[2]/header/div/div[3]/nav/ul/li[1]/div[2]/div/div/div[1]/ul/li[*]/a");

        var itens = departamentos.stream()
                .collect(Collectors.toMap(e -> e.getHrefAttribute(), e -> e.asNormalizedText()));

//        List<Future<List<ScrapingItem>>> futures = new ArrayList<>();
//        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (var item : itens.entrySet()) {

            String link = item.getKey();
            String deptName = item.getValue();

            response.addAll(executeDept(link, deptName));

//            Future<List<ScrapingItem>> future = executor.submit(() -> executeDept(link, deptName));
//            futures.add(future);
        }

//        executor.shutdown();
//
//        futures.forEach(f -> {
//            try {
//                response.addAll(f.get());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });

        System.out.println(String.format("Result: %s", response.getItens().size()));

        return response;
    }

    private List<ScrapingItemDto> executeDept(String link, String deptName) throws InterruptedException {

        List<ScrapingItemDto> result = new ArrayList<>();
        HtmlPage page;

        int numberPage = 1;
        boolean goToNextPage = true;
        int retry = 1;

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

                    result.add(ScrapingItemDto.build(company, deptName, productName, parsePrice(descricaoPreco)));
                }

                goToNextPage = !CollectionUtils.isEmpty(produtos);

                numberPage++;
            } catch (FailingHttpStatusCodeException e) {
                if (e.getStatusCode() != 404 && retry < 3) {
                    Thread.sleep(2_000);
                } else
                    goToNextPage = false;

                retry++;
            } catch (Exception e) {
                goToNextPage = false;
            }
        }

        return result;
    }

    private BigDecimal parsePrice(String priceAsString) {
        priceAsString = priceAsString.replaceAll("[^0-9,]", "")
                .replaceAll(",", ".");
        return new BigDecimal(priceAsString);
    }
}