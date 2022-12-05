package br.com.gredom.webscraping.entity;

import br.com.gredom.webscraping.enums.Company;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "item_category")
@Getter
public class ItemCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Company company;

    @Setter
    private String description;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category", referencedColumnName = "id")
    private CategoryEntity category;

    @Setter
    private BigDecimal originalPrice;

    @Setter
    private String installment;

    @Setter
    private BigDecimal bestPrice;

    @Setter
    private String bestPriceMethod;

    @Setter
    private String urlItem;

    @CreationTimestamp
    private OffsetDateTime created;

    @UpdateTimestamp
    private OffsetDateTime updated;

    public ItemCategoryEntity() {
    }

    public ItemCategoryEntity(Company company, String urlItem) {
        this.company = company;
        this.urlItem = urlItem;
    }
}