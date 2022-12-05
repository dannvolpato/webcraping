package br.com.gredom.webscraping.repository.impl;

import br.com.gredom.webscraping.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByUrl(String code);
}