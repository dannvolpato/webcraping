package br.com.gredom.webscraping.scraping;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RestController
@RequiredArgsConstructor
public class Scraping {

    private final WebClient webClient;

    @GetMapping
    public void execute() throws Exception {

        List<ScrapingResult> result = new ArrayList<>();

        String baseUrl = "https://www.magazineluiza.com.br/";
        HtmlPage page = webClient.getPage(baseUrl);

        List<HtmlAnchor> departamentos = page.getByXPath("//*[@id=\"__next\"]/div/main/section[1]/div[2]/header/div/div[3]/nav/ul/li[1]/div[2]/div/div/div[1]/ul/li[*]/a");

        var itens = departamentos.stream()
                .collect(Collectors.toMap(e -> e.getHrefAttribute(), e -> e.asNormalizedText()));

        for (var item : itens.entrySet()) {

            var link = item.getKey();
            var deptName = item.getValue();

            int numberPage = 1;
            boolean hasPage = true;

            while (hasPage) {
                String linkPaginado = String.format("%s%s%s", link, "?page=", numberPage);
                System.out.println(linkPaginado);
                try {
                    page = webClient.getPage(linkPaginado);
                } catch (Exception e) {
                    hasPage = false;
                    continue;
                }

                List<HtmlElement> produtos = page.getByXPath("//div[@data-testid=\"product-card-content\"]");

                for (var produto : produtos) {
                    HtmlHeading2 h2 = produto.getFirstByXPath("./h2");
                    var descricaoProduto = h2.asNormalizedText();

                    HtmlParagraph p = produto.getFirstByXPath(".//p[@data-testid=\"price-value\"]");

                    var descricaoPreco = p.asNormalizedText();

                    result.add(ScrapingResult.build(deptName, descricaoProduto, descricaoPreco));
                }

                hasPage = !CollectionUtils.isEmpty(produtos);

                numberPage++;
            }
        }
    }
}