package br.com.gredom.webscraping.util;

import br.com.gredom.webscraping.entity.CategoryEntity;

import java.util.List;

public class Collections {
    public static boolean nonEmpty(List<CategoryEntity> categories) {
        return categories != null && categories.size() > 0;
    }
}