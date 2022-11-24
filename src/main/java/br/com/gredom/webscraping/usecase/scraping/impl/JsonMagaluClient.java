package br.com.gredom.webscraping.usecase.scraping.impl;

import br.com.gredom.webscraping.response.JsonMagalu;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "magalu", url = "${application.scraping.magalu.homepage}${application.scraping.magalu.path-index-json}")
public interface JsonMagaluClient {

    @GetMapping(path = "/index.json")
    JsonMagalu getIndex();

    @GetMapping(path = "/{path}.json")
    JsonMagalu getByPath(@PathVariable String path);

    @GetMapping(path = "/{path}.json?page={page}&path0={path0}&path2={path2}")
    JsonMagalu getByPath(@PathVariable String path, @PathVariable int page, @PathVariable String path0, @PathVariable String path2);
}