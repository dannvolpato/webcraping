package br.com.gredom.webscraping.usecase.scraping.magalu;

import br.com.gredom.webscraping.enums.Company;
import br.com.gredom.webscraping.response.ScrapingResponse;
import br.com.gredom.webscraping.util.GerarArquivo;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapingMagaluSelenium {

    private static final Company company = Company.MAGALU;
    private static final String baseUrl = "https://www.magazineluiza.com.br";
    private final GerarArquivo gerarArquivo;

    public ScrapingResponse execute() throws Exception {

        double start = System.currentTimeMillis();
        ScrapingResponse response = ScrapingResponse.build();

        FirefoxOptions options = new FirefoxOptions();
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36 OPR/60.0.3255.170";
        options.addPreference("general.useragent.override", userAgent);

        WebDriverManager.firefoxdriver().setup();
        WebDriver driver = new FirefoxDriver(options);

        try {
            scrapingMagaluSelenium(driver, response);
        } finally {
            driver.quit();
        }

        gerarArquivo.execute(response);

        double time = (System.currentTimeMillis() - start) / 1_000;
        long minutes = (long) (time / 60);
        double seconds = time - (minutes * 60);
        System.out.println(String.format("Result: %s, duration: %s:%s", response.getItens().size(), minutes, seconds));

        return response;
    }

    private void scrapingMagaluSelenium(WebDriver driver, ScrapingResponse response) {

        driver.get(baseUrl);

        List<WebElement> webDepartamentos = driver.findElements(By.xpath("/html/body/div[1]/div/main/section[1]/div[2]/header/div/div[3]/nav/ul/li[1]/div[2]/div/div/div[1]/ul/li[*]"));

        List<String> linksDepartamentos = webDepartamentos.stream()
                .map(element -> extractDepartamento(element))
                .collect(Collectors.toList());

        for (var dept : linksDepartamentos) {
//            driver.get(dept);
//            driver.getCurrentUrl();
            System.out.println(dept);
        }
    }

    private String extractDepartamento(WebElement element) {
        return element.findElement(By.tagName("a")).getAttribute("href");
    }
}