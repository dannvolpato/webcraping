package br.com.gredom.webscraping.scraping;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor()
@AllArgsConstructor(staticName = "build")
public class ScrapingResult {

    private String dept;
    private String description;
    private String price;

    public static ScrapingResult build(String dept, String description, String price) {
        return new ScrapingResult(dept, description, price);
    }
}