package br.com.gredom.webscraping.response.magalu;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Data {
    private List<Category> allCategories;
    private Search search;
}