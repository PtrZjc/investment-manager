package pl.zajacp.investmentmanager.actionmanagement;

import lombok.Data;
import pl.zajacp.investmentmanager.investments.FinanceProduct;
import pl.zajacp.investmentmanager.investments.Investment;
import pl.zajacp.investmentmanager.user.User;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
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
    @NotNull
    private BigDecimal balanceChange;
    @NotNull
    private BigDecimal afterActionValue;
    @NotNull
    private Boolean isDone;
    @Column(length = 1000)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private FinanceProduct product;

    public Action() {
        this.isDone = false;
    }

}