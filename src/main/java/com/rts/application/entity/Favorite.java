package com.rts.application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "favorites", uniqueConstraints = {
        @UniqueConstraint(name = "uk_favorites_user_ticker", columnNames = {"user_id", "ticker"})
})
@Getter
@Setter
@NoArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String ticker;

    @Column(name = "opening_price", nullable = false)
    private BigDecimal openingPrice;

    public Favorite(User user, String ticker, BigDecimal openingPrice) {
        this.user = user;
        this.ticker = ticker;
        this.openingPrice = openingPrice;
    }
}
