package pl.zajacp.investmentmanager.actionmanagement;

import lombok.Data;
import pl.zajacp.investmentmanager.investments.FinanceProduct;
import pl.zajacp.investmentmanager.investments.Investment;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
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

    public Action() {
        this.isDone = false;
    }

}