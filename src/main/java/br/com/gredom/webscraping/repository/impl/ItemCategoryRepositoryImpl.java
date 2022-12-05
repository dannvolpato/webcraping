package br.com.gredom.webscraping.repository.impl;

import br.com.gredom.webscraping.entity.ItemCategoryEntity;
import br.com.gredom.webscraping.repository.ItemCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class ItemCategoryRepositoryImpl implements ItemCategoryRepository {

    private final ItemCategoryJpaRepository itemCategoryJpaRepository;

    @Override
    public ItemCategoryEntity save(ItemCategoryEntity entity) {
        return itemCategoryJpaRepository.save(entity);
    }

    @Override
    public Optional<ItemCategoryEntity> findByUrlItem(String urlItem) {
        return itemCategoryJpaRepository.findByUrlItem(urlItem);
    }
}