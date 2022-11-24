package br.com.gredom.webscraping.response.magalu;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class Price {
    private BigDecimal price;
}