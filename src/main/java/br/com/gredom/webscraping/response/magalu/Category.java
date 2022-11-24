package br.com.gredom.webscraping.response.magalu;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Category {
    private String id;
    private String description;
    private Meta meta;
    private String name;
}