package br.com.gredom.webscraping.usecase.scraping.magalu;

import br.com.gredom.webscraping.dto.ScrapingItemDto;
import br.com.gredom.webscraping.enums.Company;
import br.com.gredom.webscraping.response.ScrapingResponse;
import br.com.gredom.webscraping.util.GerarArquivo;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapingMagaluHtmlunit {

    private static final Company company = Company.MAGALU;
    private static final String baseUrl = "https://www.magazineluiza.com.br";

    private final WebClient webClient;
    private final GerarArquivo gerarArquivo;

    public ScrapingResponse execute() throws Exception {

        double start = System.currentTimeMillis();
        ScrapingResponse response = ScrapingResponse.build();

        HtmlPage page = webClient.getPage(baseUrl);

        List<HtmlAnchor> departamentos = page.getByXPath("//*[@id=\"__next\"]/div/main/section[1]/div[2]/header/div/div[3]/nav/ul/li[1]/div[2]/div/div/div[1]/ul/li[*]/a");

        List<String> itens = departamentos.stream()
                .map(i -> i.getHrefAttribute())
                .collect(Collectors.toList());

        for (var link : itens) {

            System.out.println(link);
            //            links.addAll(
//                    findSubCategories(link, ""));
        }
//        for (var link : links) {
//            response.addAll(
//                    executeCategory(link, "", ""));
//        }

        gerarArquivo.execute(response);

        double time = (System.currentTimeMillis() - start) / 1_000;
        long minutes = (long) (time / 60);
        double seconds = time - (minutes * 60);
        System.out.println(String.format("Result: %s, duration: %s:%s", response.getItens().size(), minutes, seconds));

        return response;
    }

    private List<String> findSubCategories(String link, String deptCode) throws Exception {

        List<String> links = new ArrayList<>();
        System.out.println(link);

        HtmlPage page = webClient.getPage(link);
//        Map<String, String> subCategorias = page.getByXPath("//div[@data-testid=\"accordion-hierarchical-filters\"]/div[2]/ul[1]/li[*]/a[@data-testid=\"list-item\"]")
//                .stream()
//                .map(i -> (HtmlAnchor) i)
//                .filter(i -> i.getHrefAttribute().contains(String.format("/%s/", deptCode)))
//                .collect(Collectors.toMap(a -> baseUrl + a.getHrefAttribute(), a -> a.asNormalizedText()));
//
//        for (var entry : subCategorias.entrySet()) {
//            links.addAll(
//                    findSubCategories(entry.getKey(), deptCode));
//        }
//
//        if (CollectionUtils.isEmpty(subCategorias))
//            links.add(link);

        var subCategorias = page.getByXPath("//div[@data-testid=\"accordion-hierarchical-filters\"]/div[2]/ul[1]/li[*]/a[@data-testid=\"list-item\"]")
                .stream()
                .map(i -> baseUrl + ((HtmlAnchor) i).getHrefAttribute())
                .filter(i -> i.contains(String.format("/%s/", deptCode)))
                .collect(Collectors.toList());

        links.addAll(subCategorias);

        return links;
    }

    private List<ScrapingItemDto> executeCategory(String link, String deptName, String deptCode) throws InterruptedException {

        List<ScrapingItemDto> result = new ArrayList<>();

        int numberPage = 1;
        boolean goToNextPage = true;
        int retry = 1;
        int produtosEncontrados = 0;
        int totalProdutos = 0;

        while (goToNextPage) {
            String linkPaginado = String.format("%s%s%s", link, "?page=", numberPage);
            System.out.println(linkPaginado);
            try {
                HtmlPage page = webClient.getPage(linkPaginado);

////                if (!page.getUrl().toString().contains(linkPaginado))
////                    throw new Exception(String.format("A página recebida é diferente da página informada: %", linkPaginado));
////
////                if (numberPage == 1)
////                    totalProdutos = extractTotalProdutos(page);
////
////                List<HtmlAnchor> produtos = page.getByXPath("//a[@data-testid=\"product-card-container\"]");
////
////                for (var produto : produtos) {
////                    String productName = extractProductName(produto);
////                    BigDecimal price = extractPrice(produto);
////                    String installment = extractInstallment(produto);
////                    String pix = extractPix(produto);
////                    String urlProduct = baseUrl + produto.getHrefAttribute();
////
////                    result.add(ScrapingItemDto.build(company, deptName, deptCode, productName, price, installment, pix, urlProduct));
////                }
////
////                produtosEncontrados += produtos.size();
//
//                goToNextPage = !CollectionUtils.isEmpty(produtos) && produtosEncontrados <= totalProdutos;
//                goToNextPage = !CollectionUtils.isEmpty(produtos) && produtosEncontrados <= totalProdutos;

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

        if (CollectionUtils.isEmpty(result))
            log.warn(String.format("Não foi possível extrair dados de produtos na página: %s", link));

        return result;
    }

    private int extractTotalProdutos(HtmlPage page) {
        try {
            HtmlParagraph pTotalProdutos = page.getFirstByXPath("//div[@data-testid=\"mod-searchheader\"]/div[1]/p");
            return Integer.parseInt(pTotalProdutos.asNormalizedText()
                    .replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            log.warn("Não foi possível extrair o total de produtos encontrados", e);
        }
        return 0;
    }

    private String extractPix(HtmlAnchor produto) {
        try {
            HtmlSpan sPix = produto.getFirstByXPath(".//span[2]");
            return sPix.asNormalizedText().replaceAll("[()]", "");
        } catch (Exception e) {
        }
        return null;
    }

    private String extractInstallment(HtmlAnchor produto) {
        try {
            HtmlParagraph pInstallment = produto.getFirstByXPath(".//p[@data-testid=\"installment\"]");
            return pInstallment.asNormalizedText();
        } catch (Exception e) {
        }
        return null;
    }

    private BigDecimal extractPrice(HtmlAnchor produto) {
        try {
            HtmlParagraph p = produto.getFirstByXPath(".//p[@data-testid=\"price-value\"]");
            var descricaoPreco = p.asNormalizedText();
            return parsePrice(descricaoPreco);
        } catch (Exception e) {
        }
        return null;
    }

    private String extractProductName(HtmlAnchor produto) {
        try {
            HtmlHeading2 h2 = produto.getFirstByXPath(".//h2[@data-testid=\"product-title\"]");
            return h2.asNormalizedText();
        } catch (Exception e) {
        }
        return null;
    }

    private BigDecimal parsePrice(String priceAsString) {
        try {
            priceAsString = priceAsString.replaceAll("[^0-9,]", "")
                    .replaceAll(",", ".");
            return new BigDecimal(priceAsString);
        } catch (Exception e) {
        }
        return null;
    }
}