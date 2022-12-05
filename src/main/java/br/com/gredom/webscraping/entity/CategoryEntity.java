package br.com.gredom.webscraping.entity;

import br.com.gredom.webscraping.enums.Company;
import br.com.gredom.webscraping.enums.StatusUrl;
import lombok.Getter;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "category")
@Getter
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Company company;

    private String name;

    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "st_url")
    private StatusUrl statusUrl;

    private BigInteger level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent", referencedColumnName = "id")
    private CategoryEntity parent;

    public CategoryEntity() {
    }

    public CategoryEntity(Company company, String name, String url, StatusUrl statusUrl, BigInteger level, CategoryEntity parent) {
        this.company = company;
        this.name = name;
        this.url = url;
        this.statusUrl = statusUrl;
        this.level = level;
        this.parent = parent;
    }

    public void modify(Company company, String name, String url, StatusUrl statusUrl, BigInteger level, CategoryEntity parent) {
        this.company = company;
        this.name = name;
        this.url = url;
        this.statusUrl = statusUrl;
        this.level = level;
        this.parent = parent;
    }
}