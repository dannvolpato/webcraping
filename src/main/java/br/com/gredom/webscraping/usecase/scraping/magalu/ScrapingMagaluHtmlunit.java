package br.com.gredom.webscraping.usecase.scraping.magalu;

import br.com.gredom.webscraping.entity.CategoryEntity;
import br.com.gredom.webscraping.enums.Company;
import br.com.gredom.webscraping.enums.StatusUrl;
import br.com.gredom.webscraping.repository.CategoryRepository;
import br.com.gredom.webscraping.response.ScrapingResponse;
import br.com.gredom.webscraping.util.GerarArquivo;
import br.com.gredom.webscraping.util.Strings;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapingMagaluHtmlunit {

    private static final Company company = Company.MAGALU;
    private static final String baseUrl = "https://www.magazineluiza.com.br";
    private final CategoryRepository categoryRepository;

    private final GerarArquivo gerarArquivo;

    public ScrapingResponse execute() throws Exception {
        double start = System.currentTimeMillis();

        ScrapingResponse response = ScrapingResponse.build();

        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);

        var inputsDept = scrapingDepartaments(webClient);

        save(inputsDept);

        for (var map : inputsDept.entrySet()) {

            String urlCategory = map.getKey();
            String code = urlCategory.split("/")[3];
            List<UrlDto> allCategories = new ArrayList<>();
            allCategories.addAll(
                    scrapingCategories(webClient, urlCategory, code));
        }

//
//        gerarArquivo.execute(response);
//
//        double time = (System.currentTimeMillis() - start) / 1_000;
//        long minutes = (long) (time / 60);
//        double seconds = time - (minutes * 60);
//        System.out.printf("Result: %s, duration: %s:%s%n", response.getItens().size(), minutes, seconds);

        return response;
    }

    private Map<String, String> scrapingDepartaments(WebClient webClient) throws Exception {

        Map<String, String> depts = new HashMap<>();
        HtmlPage page = getPage(webClient, baseUrl)
                .orElse(null);
        if (page == null)
            return depts;

        depts = page.getByXPath("//*[@id=\"__next\"]/div/main/section[1]/div[2]/header/div/div[3]/nav/ul/li[1]/div[2]/div/div/div[1]/ul/li[*]/a")
                .stream()
                .map(i -> (HtmlAnchor) i)
                .filter(i -> i.getHrefAttribute().contains(baseUrl))
                .collect(Collectors.toMap(i -> i.getHrefAttribute().split("\\?")[0], i -> i.asNormalizedText(), (a, b) -> a));

        return depts;
    }

    private List<UrlDto> scrapingCategories(WebClient webClient, String url, String code) throws Exception {

        List<UrlDto> categories = new ArrayList<>();

        HtmlPage page = getPage(webClient, url).orElse(null);

        if (page == null) return categories;

        if (!page.getUrl().toString().equals(url)) {
            log.info(String.format("Página solicitada: %s, página recebida: %s", url, page.getUrl().toString()));
            return categories;
        }

        List<UrlDto> subCategories = page.getByXPath("//div[@data-testid=\"accordion-hierarchical-filters\"]/div[2]/ul[1]/li[*]/a[@data-testid=\"list-item\"]")
                .stream()
                .map(i -> ((HtmlAnchor) i))
                .filter(i -> i.getHrefAttribute().contains(String.format("/%s/", code)))
                .collect(Collectors.toMap(i -> baseUrl + i.getHrefAttribute().split("\\?")[0], i -> i.asNormalizedText(), (a, b) -> a))
                .entrySet().stream()
                .map(m -> new UrlDto(m.getKey(), m.getValue(), url))
                .collect(Collectors.toList());

        log.info(String.format("%s %s", url, subCategories.size()));

        save(subCategories);

        categories.addAll(subCategories);
        for (var item : subCategories) {
            if (!url.equals(item.getUrl()))
                categories.addAll(
                        scrapingCategories(webClient, item.getUrl(), code));
        }

        return categories;
//    }
//
//    private List<ScrapingItemDto> executeCategory(String link, String deptName, String deptCode) throws InterruptedException {
//
//        List<ScrapingItemDto> result = new ArrayList<>();
//
//        int numberPage = 1;
//        boolean goToNextPage = true;
//        int retry = 1;
//        int produtosEncontrados = 0;
//        int totalProdutos = 0;
//
//        while (goToNextPage) {
//            String linkPaginado = String.format("%s%s%s", link, "?page=", numberPage);
//            System.out.println(linkPaginado);
//            try {
//                HtmlPage page = webClient.getPage(linkPaginado);
//
//////                if (!page.getUrl().toString().contains(linkPaginado))
//////                    throw new Exception(String.format("A página recebida é diferente da página informada: %", linkPaginado));
//////
//////                if (numberPage == 1)
//////                    totalProdutos = extractTotalProdutos(page);
//////
//////                List<HtmlAnchor> produtos = page.getByXPath("//a[@data-testid=\"product-card-container\"]");
//////
//////                for (var produto : produtos) {
//////                    String productName = extractProductName(produto);
//////                    BigDecimal price = extractPrice(produto);
//////                    String installment = extractInstallment(produto);
//////                    String pix = extractPix(produto);
//////                    String urlProduct = baseUrl + produto.getHrefAttribute();
//////
//////                    result.add(ScrapingItemDto.build(company, deptName, deptCode, productName, price, installment, pix, urlProduct));
//////                }
//////
//////                produtosEncontrados += produtos.size();
////
////                goToNextPage = !CollectionUtils.isEmpty(produtos) && produtosEncontrados <= totalProdutos;
////                goToNextPage = !CollectionUtils.isEmpty(produtos) && produtosEncontrados <= totalProdutos;
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
//
//        if (CollectionUtils.isEmpty(result))
//            log.warn(String.format("Não foi possível extrair dados de produtos na página: %s", link));
//
//        return result;
//    }
//
//    private int extractTotalProdutos(HtmlPage page) {
//        try {
//            HtmlParagraph pTotalProdutos = page.getFirstByXPath("//div[@data-testid=\"mod-searchheader\"]/div[1]/p");
//            return Integer.parseInt(pTotalProdutos.asNormalizedText()
//                    .replaceAll("[^0-9]", ""));
//        } catch (Exception e) {
//            log.warn("Não foi possível extrair o total de produtos encontrados", e);
//        }
//        return 0;
//    }
//
//    private String extractPix(HtmlAnchor produto) {
//        try {
//            HtmlSpan sPix = produto.getFirstByXPath(".//span[2]");
//            return sPix.asNormalizedText().replaceAll("[()]", "");
//        } catch (Exception e) {
//        }
//        return null;
//    }
//
//    private String extractInstallment(HtmlAnchor produto) {
//        try {
//            HtmlParagraph pInstallment = produto.getFirstByXPath(".//p[@data-testid=\"installment\"]");
//            return pInstallment.asNormalizedText();
//        } catch (Exception e) {
//        }
//        return null;
//    }
//
//    private BigDecimal extractPrice(HtmlAnchor produto) {
//        try {
//            HtmlParagraph p = produto.getFirstByXPath(".//p[@data-testid=\"price-value\"]");
//            var descricaoPreco = p.asNormalizedText();
//            return parsePrice(descricaoPreco);
//        } catch (Exception e) {
//        }
//        return null;
//    }
//
//    private String extractProductName(HtmlAnchor produto) {
//        try {
//            HtmlHeading2 h2 = produto.getFirstByXPath(".//h2[@data-testid=\"product-title\"]");
//            return h2.asNormalizedText();
//        } catch (Exception e) {
//        }
//        return null;
//    }
//
//    private BigDecimal parsePrice(String priceAsString) {
//        try {
//            priceAsString = priceAsString.replaceAll("[^0-9,]", "")
//                    .replaceAll(",", ".");
//            return new BigDecimal(priceAsString);
//        } catch (Exception e) {
//        }
//        return null;
//    }
    }

    private Optional<HtmlPage> getPage(WebClient webClient, String url) throws Exception {
        HtmlPage page = null;

        int sleep = 3;
        int retry = 0;
        while (page == null && retry < 5) {
            try {
                page = webClient.getPage(url);
            } catch (FailingHttpStatusCodeException e) {
                log.info("retry " + url);
                Thread.sleep(sleep * 1_000);
                sleep += 2;
            }
            retry++;
        }

        return Optional.ofNullable(page);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void save(List<UrlDto> subCategories) {

        subCategories.forEach(category -> {

            String name = category.getName();
            String url = category.getUrl();
            BigInteger level = BigInteger.ZERO;

            CategoryEntity parent = null;
            if (Strings.nonBlank(category.getUrlParent()))
                parent = categoryRepository.findByUrl(category.getUrlParent()).orElse(null);

            if (parent != null)
                level = parent.getLevel().add(BigInteger.ONE);

            CategoryEntity cat = categoryRepository.findByUrl(url)
                    .orElse(new CategoryEntity());

            cat.modify(company,
                    name,
                    url,
                    StatusUrl.ACTIVE,
                    level,
                    parent);

            categoryRepository.save(cat);

        });

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void save(Map<String, String> categoriesInput) {

        categoriesInput.entrySet().forEach(entry -> {

            String name = entry.getValue();
            String url = entry.getKey();
            BigInteger level = BigInteger.ZERO;

            CategoryEntity cat = categoryRepository.findByUrl(url)
                    .orElse(new CategoryEntity());

            CategoryEntity parent = null;

            cat.modify(company,
                    name,
                    url,
                    StatusUrl.ACTIVE,
                    level,
                    parent);

            categoryRepository.save(cat);
        });
    }

    @Getter
    private class UrlDto {
        public UrlDto(String url, String name, String urlParent) {
            this.url = url;
            this.name = name;
            this.urlParent = urlParent;
        }

        private String name;
        private String url;
        private String urlParent;
    }
}