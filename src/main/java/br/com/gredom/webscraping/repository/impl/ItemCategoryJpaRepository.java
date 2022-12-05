package br.com.gredom.webscraping.repository.impl;

import br.com.gredom.webscraping.entity.ItemCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ItemCategoryJpaRepository extends JpaRepository<ItemCategoryEntity, Long> {
    Optional<ItemCategoryEntity> findByUrlItem(String code);
}