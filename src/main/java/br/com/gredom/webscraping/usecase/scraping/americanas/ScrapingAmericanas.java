package br.com.gredom.webscraping.usecase.scraping.americanas;

import br.com.gredom.webscraping.enums.Company;
import br.com.gredom.webscraping.response.ScrapingResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapingAmericanas {

    private static final Company company = Company.AMERICANAS;
    private static final String baseUrl = "https://www.americanas.com.br";
    private final WebClient webClient;

    public ScrapingResponse execute() throws Exception {

        ScrapingResponse response = ScrapingResponse.build();

        HtmlPage page = webClient.getPage(baseUrl);

        List<String> linkCategorias = page.getAnchors().stream()
                .map(a -> a.getHrefAttribute()
                        .split("\\?")[0])
                .filter(link -> link.contains("/categoria/"))
                .map(link -> {
                    var array = link.split("/");
                    return String.format("%s%s%s", baseUrl, "/categoria/", array[4]);
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> xPaths = new ArrayList<>();
        xPaths.add("/html/body/div[2]/div[1]/div[1]/div[1]/a[*]"); // Celulares e smartphones
        xPaths.add("/html/body/div[3]/div[1]/div[1]/div[1]/a[*]"); // Moda
        xPaths.add("/html/body/div[4]/div[1]/div[1]/div[1]/a[*]"); // Utilidades domesticas
        xPaths.add("//*[@id=\"rsyswpsdk\"]/div[1]/main/div[1]/div[2]/div[1]/div[1]/div[1]/a[*]"); // Eletrodomesticos

        List<String> subcategorias = new ArrayList<>();
        for (var link : linkCategorias) {
            executeCategoria(link, xPaths, subcategorias);
        }

        List<String> linkSubcategorias = subcategorias.stream()
                .distinct()
                .map(i -> baseUrl + i)
                .sorted()
                .collect(Collectors.toList());

        for (var link : linkSubcategorias) {
            executeDept(link, response);
        }

        return response;
    }

    private void executeCategoria(String link, List<String> xPaths, List<String> linkCategoriasListagem) throws Exception {

        HtmlPage p = webClient.getPage(link);
        System.out.println(link);

        boolean asContentByXPath = false;
        for (var xPath : xPaths) {
            boolean foundXPath = executeXPath(p, xPath, linkCategoriasListagem);
            asContentByXPath = asContentByXPath || foundXPath;
        }

        if (!asContentByXPath)
            log.warn("Categorias não encontradas na página ".concat(link));
    }

    private boolean executeXPath(HtmlPage p, String xPath, List<String> linkCategoriasListagem) {

        try {
            List<HtmlAnchor> categorias = p.getByXPath(xPath);

            List<String> cat = categorias.stream()
                    .map(i -> i.getHrefAttribute())
                    .filter(i -> i.contains("/categoria/"))
                    .collect(Collectors.toList());

            linkCategoriasListagem.addAll(cat);

            return !CollectionUtils.isEmpty(cat);

        } catch (Exception e) {
            log.info("", e);
        }
        return false;
    }

    private void executeDept(String link, ScrapingResponse response) throws Exception {

        HtmlPage p = webClient.getPage(link);

        List<HtmlDivision> divsProdutos = p.getByXPath("//*[@id=\"rsyswpsdk\"]/div/main/div/div[3]/div[2]/div[*]");



        for (var div : divsProdutos) {

        }

        //        int numberPage = 1;
//        boolean goToNextPage = true;
//        int retry = 1;
//
//        while (goToNextPage) {
//            System.out.println(link);
//            try {
//                HtmlPage page = webClient.getPage(link);
//
//                List<HtmlDivision> produtos = page.getByXPath("//div[@data-testid=\"product-card-content\"]");
//
//                for (var produto : produtos) {
//                    HtmlHeading2 h2 = produto.getFirstByXPath("./h2");
//                    var productName = h2.asNormalizedText();
//
//                    HtmlParagraph p = produto.getFirstByXPath(".//p[@data-testid=\"price-value\"]");
//
//                    var descricaoPreco = p.asNormalizedText();
//
//                    result.add(ScrapingItemDto.build(company, deptName, productName, parsePrice(descricaoPreco)));
//                }
//
//                goToNextPage = !CollectionUtils.isEmpty(produtos);
//
//                numberPage++;
//            } catch (FailingHttpStatusCodeException e) {
//                if (e.getStatusCode() != 404 && retry < 3) {
//                    Thread.sleep(2_000);
//                } else
//                    goToNextPage = false;
//
//                retry++;
//            } catch (Exception e) {
//                goToNextPage = false;
//            }
//        }

    }

    private BigDecimal parsePrice(String priceAsString) {
        priceAsString = priceAsString.replaceAll("[^0-9,]", "")
                .replaceAll(",", ".");
        return new BigDecimal(priceAsString);
    }
}