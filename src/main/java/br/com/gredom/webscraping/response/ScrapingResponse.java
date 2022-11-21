package br.com.gredom.webscraping.response;

import br.com.gredom.webscraping.dto.ScrapingItemDto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ScrapingResponse {

    private List<ScrapingItemDto> itens = new ArrayList<>();

    private ScrapingResponse() {
    }

    public static ScrapingResponse build() {
        return new ScrapingResponse();
    }

    public void add(ScrapingItemDto item) {
        itens.add(item);
    }

    public void addAll(List<ScrapingItemDto> scrapingItemDtos) {
        itens.addAll(scrapingItemDtos);
    }
}