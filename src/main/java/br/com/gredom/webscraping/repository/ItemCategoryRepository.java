package br.com.gredom.webscraping.repository;

import br.com.gredom.webscraping.entity.ItemCategoryEntity;

import java.util.Optional;

public interface ItemCategoryRepository {

    ItemCategoryEntity save(ItemCategoryEntity entity);

    Optional<ItemCategoryEntity> findByUrlItem(String url);
}