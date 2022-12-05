package br.com.gredom.webscraping.repository.impl;

import br.com.gredom.webscraping.entity.CategoryEntity;
import br.com.gredom.webscraping.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public CategoryEntity save(CategoryEntity entity) {
        return categoryJpaRepository.save(entity);
    }

    @Override
    public Optional<CategoryEntity> findByUrl(String url) {
        return categoryJpaRepository.findByUrl(url);
    }
}