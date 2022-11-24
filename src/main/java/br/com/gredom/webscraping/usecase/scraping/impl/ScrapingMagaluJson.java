package br.com.gredom.webscraping.usecase.scraping.impl;

import br.com.gredom.webscraping.dto.ScrapingItemDto;
import br.com.gredom.webscraping.enums.Company;
import br.com.gredom.webscraping.response.JsonMagalu;
import br.com.gredom.webscraping.response.ScrapingResponse;
import br.com.gredom.webscraping.util.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapingMagaluJson {

    public static final Company company = Company.MAGALU;
    private final JsonMagaluClient magaluClient;

    @Value("${application.scraping.magalu.homepage}")
    private String urlHomePage;

    public ScrapingResponse execute() throws Exception {

        ScrapingResponse response = ScrapingResponse.build();
        int poolRequests = 10;
        int qtRequest = 0;

        var index = magaluClient.getIndex();
        qtRequest++;

        List<String> pathsDept = index.getPageProps().getData().getAllCategories()
                .stream()
                .map(i -> {
                    String url = i.getMeta().getCanonical().replaceAll(urlHomePage, "");
                    if (url.endsWith("/"))
                        url = StringUtils.removeEnd(url, "/");
                    if (url.startsWith("/"))
                        url = StringUtils.removeStart(url, "/");
                    return url;
                })
                .filter(path -> Strings.nonBlank(path))
                .collect(Collectors.toList());

        for (int i = 0; i < pathsDept.size(); i++) {
            String path = pathsDept.get(i);
            String[] splitedPath = path.split("/");

            int page = 1;
            int pages = page;
            while (page <= pages) {
                JsonMagalu dept = magaluClient.getByPath(path, page, splitedPath[0], splitedPath[2]);
                qtRequest++;

                int qtProdutos = dept.getPageProps().getData().getSearch().getProducts().size();

                System.out.println(String.format("%s - page %s - found: %s", path, page, qtProdutos));

                pages = dept.getPageProps().getData().getSearch().getPagination().getPages();
                if (qtRequest % poolRequests == 0) Thread.sleep(10_000);
                Thread.sleep(2_000);

                List<ScrapingItemDto> products = dept.getPageProps().getData().getSearch().getProducts().stream()
                        .map(p -> ScrapingItemDto.build(company, "", "", p.getTitle(), p.getPrice().getPrice(), "", "", ""))
                        .collect(Collectors.toList());

                response.addAll(products);
                page++;
            }
        }
        System.out.println(qtRequest);

        return response;
    }
}