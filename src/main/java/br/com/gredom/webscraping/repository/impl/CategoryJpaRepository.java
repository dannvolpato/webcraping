package br.com.gredom.webscraping.repository.impl;

import br.com.gredom.webscraping.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByUrl(String code);

    @Query(value = "select * from category where selected = true order by :orderBy offset :offset limit :limit", nativeQuery = true)
    List<CategoryEntity> findSelecteds(String orderBy, int offset, int limit);
}