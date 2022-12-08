package br.com.gredom.webscraping.usecase.scraping;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class SeleniumClient {

    public void execute(WebDriver driver) throws Exception {

        String url = "https://www.casasbahia.com.br/c/eletrodomesticos/refrigeradores/2-portas?filtro=c13_c14_c143&nid=201463";

        driver.get(url);


        WebElement button = driver.findElement(
                By.xpath("//*[@id=\"__next\"]/main/div/div/div/div/div[2]/div/div[3]/div[2]/div[4]/div/div/button"));

        while (button != null) {
            button.click();
            try {
                button = driver.findElement(
                        By.xpath("//*[@id=\"__next\"]/main/div/div/div/div/div[2]/div/div[3]/div[2]/div[4]/div/div/button"));
            } catch (Exception e) {
                button = null;
            }
        }

        List<WebElement> elementsProdutos = driver.findElements(
                By.xpath("//*[@id=\"__next\"]/main/div/div/div/div/div[2]/div/div[3]/div[2]/div[3]/div[1]/div[*]/div/div/div[2]"));

        List<String> collect = elementsProdutos.stream()
                .map(e -> {
                    String descricaoItem = e.findElement(By.xpath("a[1]")).getText();
                    String descricaoPreco = e.findElement(By.xpath("a[2]/div/span[1]")).getText();
                    String space = "                                                                                                                 ";
                    return String.format("%s, %s", descricaoItem.concat(space).substring(0, space.length()), parsePreco(descricaoPreco));
                })
                .collect(Collectors.toList());

        driver.quit();

        collect.forEach(System.out::println);
    }

    public static void main(String[] args)  {
        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();
        SeleniumClient client = new SeleniumClient();

        try {
            client.execute(driver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            driver.close();
        }
    }

    private BigDecimal parsePreco(String precoAsString) {

        try {
            String n = precoAsString
                    .replaceAll("[^0-9,]", "")
                    .replaceAll(",", ".");

            return new BigDecimal(n);
        } catch (Exception e) {
        }
        return null;
    }
}