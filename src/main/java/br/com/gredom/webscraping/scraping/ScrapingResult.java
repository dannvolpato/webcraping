package br.com.gredom.webscraping.scraping;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "build")
public class ScrapingResult {

    private Company company;
    private String deptName;
    private String productName;
    private BigDecimal price;
}