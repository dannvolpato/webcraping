package br.com.gredom.webscraping.response.magalu;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Pagination {
    private int page;
    private int pages;
    private int records;
    private int size;
    private int start;
}