package br.com.gredom.webscraping.repository;

import br.com.gredom.webscraping.entity.CategoryEntity;

import java.util.Optional;

public interface CategoryRepository {

    CategoryEntity save(CategoryEntity entity);

    Optional<CategoryEntity> findByUrl(String url);
}
