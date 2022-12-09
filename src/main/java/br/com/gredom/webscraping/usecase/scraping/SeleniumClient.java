package br.com.gredom.webscraping.usecase.scraping;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class SeleniumClient {

    public static void main(String[] args) {

        String url = "https://www.casasbahia.com.br/c/eletrodomesticos/refrigeradores/2-portas?filtro=c13_c14_c143&nid=201463";

        SeleniumClient client = new SeleniumClient();
        WebDriver webDriver = client.newWebClient();

        try {
            client.execute(webDriver, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            webDriver.close();
        }
    }

    public WebDriver newWebClient() {
        WebDriverManager.chromedriver().setup();
        WebDriver webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();
        return webDriver;
    }

    public void execute(WebDriver webClient, String url) throws Exception {

        List<String> collect = new ArrayList<>();

        webClient.get(url);

        boolean proximaPagina = false;

        do {

            List<WebElement> elementsProdutos = webClient.findElements(
                    By.xpath("//*[@id=\"__next\"]/main/div/div/div/div/div[2]/div/div[3]/div[2]/div[3]/div[1]/div[*]/div/div/div[2]"));

            List<String> itensPage = elementsProdutos.stream()
                    .map(e -> {
                        String descricaoItem = e.findElement(By.xpath("a[1]")).getText();
                        String descricaoPreco = e.findElement(By.xpath("a[2]/div/span[1]")).getText();
                        String space = "                                                                                                                 ";
                        return String.format("%s, %s", descricaoItem.concat(space).substring(0, space.length()), parsePreco(descricaoPreco));
                    })
                    .collect(Collectors.toList());

            collect.addAll(itensPage);

            var next = extrairBotaoProximaPagina(webClient).orElse(null);

            if (next != null)
                next.click();

            proximaPagina = next != null;

        } while (proximaPagina);

        //        webClietn.get(url);
//
//        WebElement button = webClietn.findElement(
//                By.xpath("//*[@id=\"__next\"]/main/div/div/div/div/div[2]/div/div[3]/div[2]/div[4]/div/div/button"));
//
//        while (button != null) {
//            button.click();
//            try {
//                button = webClietn.findElement(
//                        By.xpath("//*[@id=\"__next\"]/main/div/div/div/div/div[2]/div/div[3]/div[2]/div[4]/div/div/button"));
//            } catch (Exception e) {
//                button = null;
//            }
//        }

        collect.forEach(System.out::println);
    }

    private Optional<WebElement> extrairBotaoProximaPagina(WebDriver webClient) {
        WebElement botao = null;
        try {
            botao = webClient.findElement(By.xpath("//*[@id=\"__next\"]/main/div/div/div/div/div[2]/div/div[3]/div[2]/nav/ul/a"));
        } catch (Exception e) {
        }
        return Optional.ofNullable(botao);
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