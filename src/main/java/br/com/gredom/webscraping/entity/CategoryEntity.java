package br.com.gredom.webscraping.entity;

import br.com.gredom.webscraping.enums.Company;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.OffsetDateTime;

@Entity
@Table(name = "category")
@Getter
@Setter
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Company company;

    private String name;

    private String url;

    private BigInteger level;

    private Boolean selected;

    @CreationTimestamp
    private OffsetDateTime created;

    @UpdateTimestamp
    private OffsetDateTime updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent", referencedColumnName = "id")
    private CategoryEntity parent;

    public CategoryEntity() {
    }

    public CategoryEntity(Company company, String url, boolean selected) {
        this.company = company;
        this.url = url;
        this.selected = selected;
    }

    public CategoryEntity(Company company, String name, String url, BigInteger level, boolean selected, CategoryEntity parent) {
        this.company = company;
        this.name = name;
        this.url = url;
        this.level = level;
        this.selected = selected;
        this.parent = parent;
    }

}