package br.com.gredom.webscraping.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Company {

    MAGALU("Magalu"),
    AMERICANAS("Americanas");

    private final String descricao;
}