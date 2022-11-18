package br.com.gredom.webscraping.scraping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Company {

    MAGALU("Magalu");

    private final String descricao;
}
