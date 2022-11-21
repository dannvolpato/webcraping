package br.com.gredom.webscraping.dto;

import br.com.gredom.webscraping.enums.Company;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "build")
public class ScrapingItemDto {

    private Company company;
    private String deptName;
    private String productName;
    private BigDecimal price;
}