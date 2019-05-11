package pl.zajacp.investmentmanager.actionmanagement;

import lombok.Data;
import pl.zajacp.investmentmanager.products.FinanceProduct;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name="actions")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private ActionType actionType;
    @NotNull
    private LocalDate actionDate;

    @DecimalMin("0.0001")
    @DecimalMax("1")
    @Column(precision = 5, scale = 4)
    private BigDecimal capitalizationRate;
    private BigDecimal balanceChange;
    private BigDecimal afterActionValue;

    @Column(length = 1000)
    private String notes;

    private Boolean isDone;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private FinanceProduct product;

}

/*
 ***  deposit/widthdraw

 actionDate
 balanceChange


 ***  capitalization

 actionDate
 capitalizationRate
 afterActionValue
 isDone

 ***  product open

 actionDate
 value

 ***  product close

 actionDate
 value
 */