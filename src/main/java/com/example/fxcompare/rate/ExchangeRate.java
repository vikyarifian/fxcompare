package com.example.fxcompare.rate;import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "kurs_harian",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tanggal_currency_pair", columnNames = {"tanggal", "currency_pair"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tanggal", nullable = false)
    private LocalDate tanggal;

    @Column(name = "currency_pair", nullable = false, length = 7)
    private String currencyPair;

    @Column(name = "rate_bi", nullable = false, precision = 15, scale = 4)
    private BigDecimal rateBi;

    @Column(name = "rate_commercial", nullable = false, precision = 15, scale = 4)
    private BigDecimal rateCommercial;

    @Column(name = "source_bank", nullable = false, length = 50)
    private String sourceBank;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
