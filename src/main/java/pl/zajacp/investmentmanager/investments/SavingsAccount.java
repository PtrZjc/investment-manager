package pl.zajacp.investmentmanager.investments;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@DiscriminatorValue("savings account")
public class SavingsAccount extends FinanceProduct {

    @Min(1)
    private Long valueLimit;
    @Future
    private LocalDate validityDate;
    @Column(precision = 5, scale=4)
    @DecimalMin("0.0001")
    @DecimalMax("1")
    private BigDecimal interestAboveLimit;
}
