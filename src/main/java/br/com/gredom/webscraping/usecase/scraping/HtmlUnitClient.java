package br.com.gredom.webscraping.usecase.scraping;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.net.URL;

public class HtmlUnitClient {

    public HtmlPage getPage(String url) throws Exception {

        URL uri = new URL(url);
        WebRequest request = new WebRequest(uri);

        WebClient webClient = new WebClient(BrowserVersion.CHROME);

        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.waitForBackgroundJavaScript(10_000);

        NicelyResynchronizingAjaxController ajaxController = new NicelyResynchronizingAjaxController();

        webClient.setAjaxController(ajaxController);

        HtmlPage page = webClient.getPage(url);

        ajaxController.processSynchron(page, request, true);

        for (int i = 0; i < 20; i++) {
            synchronized (page) {
                page.wait(500);
            }
        }

        //        webClient.close();
        return page;
    }

    public static void main(String[] args) throws Exception {
        String url = "https://www.casasbahia.com.br/c/eletrodomesticos/refrigeradores/2-portas?filtro=c13_c14_c143&nid=201463";

        HtmlUnitClient client = new HtmlUnitClient();

        HtmlPage page = client.getPage(url);

        System.out.println(page.asXml());

    }
}