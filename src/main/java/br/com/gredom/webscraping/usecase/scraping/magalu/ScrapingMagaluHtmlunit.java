package br.com.gredom.webscraping.usecase.scraping.magalu;

import br.com.gredom.webscraping.entity.CategoryEntity;
import br.com.gredom.webscraping.entity.ItemCategoryEntity;
import br.com.gredom.webscraping.enums.Company;
import br.com.gredom.webscraping.repository.CategoryRepository;
import br.com.gredom.webscraping.repository.ItemCategoryRepository;
import br.com.gredom.webscraping.response.ScrapingResponse;
import br.com.gredom.webscraping.util.GerarArquivo;
import br.com.gredom.webscraping.util.Strings;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapingMagaluHtmlunit {

    private static final Company company = Company.MAGALU;
    private static final String baseUrl = "https://www.magazineluiza.com.br";
    private final CategoryRepository categoryRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    private final GerarArquivo gerarArquivo;

    public ScrapingResponse execute() throws Exception {
        double start = System.currentTimeMillis();

        ScrapingResponse response = ScrapingResponse.build();

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);

        var inputsDept = scrapingDepartaments(webClient);

        save(inputsDept);

        for (var map : inputsDept.entrySet()) {

            String urlCategory = map.getKey();
            String code = urlCategory.split("/")[3];
            List<CategoryDto> allCategories = new ArrayList<>();
            allCategories.addAll(
                    scrapingCategories(webClient, urlCategory, code));
        }

//
//        gerarArquivo.execute(response);
//
        double time = (System.currentTimeMillis() - start) / 1_000;
        long minutes = (long) (time / 60);
        double seconds = time - (minutes * 60);
        System.out.printf("Result: %s, duration: %s:%s%n", response.getItens().size(), minutes, seconds);

        return response;
    }

    private Map<String, String> scrapingDepartaments(WebClient webClient) throws Exception {

        Map<String, String> depts = new HashMap<>();
        HtmlPage page = getPage(webClient, baseUrl)
                .orElse(null);
        if (page == null) return depts;

        depts = page.getByXPath("//*[@id=\"__next\"]/div/main/section[1]/div[2]/header/div/div[3]/nav/ul/li[1]/div[2]/div/div/div[1]/ul/li[*]/a")
                .stream()
                .map(i -> (HtmlAnchor) i)
                .filter(i -> i.getHrefAttribute().contains(baseUrl))
//                .filter(i -> i.getHrefAttribute().contains("/eletrodomesticos/"))
                .collect(Collectors.toMap(i -> i.getHrefAttribute().split("\\?")[0], i -> i.asNormalizedText(), (a, b) -> a));

        return depts;
    }

    private List<CategoryDto> scrapingCategories(WebClient webClient, String url, String code) throws Exception {

        List<CategoryDto> categories = new ArrayList<>();

        HtmlPage page = getPage(webClient, url).orElse(null);

        if (page == null) return categories;

        List<CategoryDto> subCategories = page.getByXPath("//div[@data-testid=\"accordion-hierarchical-filters\"]/div[2]/ul[1]/li[*]/a[@data-testid=\"list-item\"]")
                .stream()
                .map(i -> ((HtmlAnchor) i))
                .filter(i -> i.getHrefAttribute().contains(String.format("/%s/", code)))
                .collect(Collectors.toMap(i -> baseUrl + i.getHrefAttribute().split("\\?")[0], i -> i.asNormalizedText(), (a, b) -> a))
                .entrySet().stream()
                .map(m -> new CategoryDto(m.getKey(), m.getValue(), url))
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
    }

    public void scrapingItems(WebClient webClient, String url, String deptName) throws Exception {

        int numberPage = 1;
        boolean tryNextPage = true;

        List<ItemCategoryDto> itens = new ArrayList<>();

        while (tryNextPage) {
            String linkPaginado = String.format("%s%s%s", url, "?page=", numberPage);
            System.out.println(linkPaginado);

            HtmlPage page = getPage(webClient, linkPaginado).orElse(null);

            List<HtmlAnchor> htmlProdutos = new ArrayList<>();
            if (page != null)
                htmlProdutos = page.getByXPath("//a[@data-testid=\"product-card-container\"]");

            for (var htmlProduto : htmlProdutos) {
                String productName = extractProductName(htmlProduto);
                BigDecimal originalPrice = extractOriginalPrice(htmlProduto);
                String installment = extractInstallment(htmlProduto);

                BigDecimal bestPrice = extractPriceValue(htmlProduto);
                String inCash = extractInCash(htmlProduto);

                String urlProduct = baseUrl + htmlProduto.getHrefAttribute();

                System.out.println(String.format("%s, %s, %s, %s, %s, %s, %s, %s", company, deptName, productName, originalPrice, bestPrice, installment, inCash, urlProduct));

                itens.add(
                        ItemCategoryDto.of(productName, url, originalPrice, installment, bestPrice, inCash, urlProduct));
            }

            tryNextPage = page != null;

            numberPage++;
        }

        saveItensCategory(itens);
    }

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

    private String extractInCash(HtmlAnchor produto) {
        try {
            HtmlSpan sPix = produto.getFirstByXPath(".//span[@data-testid=\"in-cash\"]");
            return sPix.asNormalizedText();
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

    private BigDecimal extractOriginalPrice(HtmlAnchor produto) {
        try {
            HtmlParagraph p = produto.getFirstByXPath(".//p[@data-testid=\"price-original\"]");
            String priceAsString = p.asNormalizedText();
            return parsePrice(priceAsString);
        } catch (Exception e) {
        }
        return null;
    }

    private BigDecimal extractPriceValue(HtmlAnchor produto) {
        try {
            HtmlParagraph p = produto.getFirstByXPath(".//p[@data-testid=\"price-value\"]");
            String priceAsString = p.asNormalizedText();
            return parsePrice(priceAsString);
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

    private Optional<HtmlPage> getPage(WebClient webClient, String url) throws Exception {

        URL uri = new URL(url);
        WebRequest request = new WebRequest(uri);
        webClient.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:107.0) Gecko/20100101 Firefox/107.0");

        HtmlPage page = null;

        int sleep = 3;
        int contRetry = 0;
        boolean retry = true;
        while (retry) {
            try {
                page = webClient.getPage(request);

                String pageFound = page.getUrl().toString();
                if (StringUtils.endsWith(pageFound, "/")) pageFound = StringUtils.removeEnd(pageFound, "/");
                if (StringUtils.endsWith(url, "/")) url = StringUtils.removeEnd(url, "/");

                boolean redirect = !pageFound.equals(url);
                if (redirect) {
                    log.info(String.format("Página solicitada: %s, página recebida: %s", url, page.getUrl().toString()));
                    page = null;
                }
                retry = false;
            } catch (FailingHttpStatusCodeException e) {
                log.info("retry " + url);
                Thread.sleep(sleep * 1_000);
                sleep += 2;
                retry = contRetry < 5 && e.getStatusCode() != 404;
            }
            contRetry++;
        }

        return Optional.ofNullable(page);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void save(List<CategoryDto> subCategories) {

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
                    .orElse(new CategoryEntity(company, url, false));

            cat.setName(name);
            cat.setLevel(level);
            cat.setParent(parent);

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
                    .orElse(new CategoryEntity(company, url, false));

            CategoryEntity parent = null;

            cat.setName(name);
            cat.setLevel(level);
            cat.setParent(parent);

            categoryRepository.save(cat);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveItensCategory(List<ItemCategoryDto> itens) {

        itens.forEach(item -> {
            ItemCategoryEntity entity = itemCategoryRepository.findByUrlItem(item.getUrlItem())
                    .orElse(new ItemCategoryEntity(company, item.urlItem));

            CategoryEntity category = categoryRepository.findByUrl(item.getUrlCategory())
                    .orElse(null);

            entity.setDescription(item.description);
            entity.setCategory(category);
            entity.setOriginalPrice(item.originalPrice);
            entity.setInstallment(item.installment);
            entity.setBestPrice(item.bestPrice);
            entity.setBestPriceMethod(item.bestPriceMethod);
            entity.setUrlItem(item.urlItem);

            itemCategoryRepository.save(entity);
        });
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    private static class CategoryDto {
        private String url;
        private String name;
        private String urlParent;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    private static class ItemCategoryDto {
        private String description;
        private String urlCategory;
        private BigDecimal originalPrice;
        private String installment;
        private BigDecimal bestPrice;
        private String bestPriceMethod;
        private String urlItem;
    }
}