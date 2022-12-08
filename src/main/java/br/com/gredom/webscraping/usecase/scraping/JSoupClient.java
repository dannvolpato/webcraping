package br.com.gredom.webscraping.usecase.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JSoupClient {

    public Document getPage(String url) throws Exception {
        return Jsoup.connect(url)
                .timeout(10_000)
                .get();
    }

    public static void main(String[] args) throws Exception {
        String url = "https://www.casasbahia.com.br/c/eletrodomesticos/refrigeradores/2-portas?filtro=c13_c14_c143&nid=201463";

        JSoupClient m = new JSoupClient();

        Document docPage = m.getPage(url);

        Thread.sleep(5_000);

        System.out.println(docPage);
    }
}