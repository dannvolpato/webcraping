package br.com.gredom.webscraping.util;

import br.com.gredom.webscraping.dto.ScrapingItemDto;
import br.com.gredom.webscraping.response.ScrapingResponse;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GerarArquivo {

    private static String separador = ";";

    public void execute(ScrapingResponse input) throws Exception {

        LocalDateTime agora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        File dir = new File("temp");
        String filename = String.format("price_%s.txt", agora)
                .replaceAll("[:-]", "");
        File arq = new File(dir, filename);

        arq.createNewFile();

        FileWriter fileWriter = new FileWriter(arq, false);

        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.println(getCabecalhoArquivo());
        input.getItens().forEach(item -> printWriter.println(getLinhaArquivo(item)));

        printWriter.flush();

        printWriter.close();
    }

    public String getCabecalhoArquivo() {
        List<String> cabecalho = Arrays.asList(
                "Company name",
                "Dept name",
                "Product name",
                "Price",
                "Installment",
                "PIX payment",
                "URL");
        return cabecalho.stream()
                .collect(Collectors.joining(separador));
    }

    public String getLinhaArquivo(ScrapingItemDto item) {
        List<String> linha = Arrays.asList(
                Objects.nonNull(item.getCompany()) ? item.getCompany().getDescricao() : "",
                Objects.nonNull(item.getDeptName()) ? item.getDeptName() : "",
                Objects.nonNull(item.getProductName()) ? item.getProductName() : "",
                Objects.nonNull(item.getPrice()) ? item.getPrice().toString().replaceAll("\\.", ",") : "",
                Objects.nonNull(item.getInstallment()) ? item.getInstallment() : "",
                Objects.nonNull(item.getPix()) ? item.getPix() : "",
                Objects.nonNull(item.getUrlProduct()) ? item.getUrlProduct() : "");
        return linha.stream()
                .collect(Collectors.joining(separador));
    }
}