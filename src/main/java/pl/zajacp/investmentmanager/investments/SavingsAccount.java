package pl.zajacp.investmentmanager.investments;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@DiscriminatorValue("savings account")
public class SavingsAccount extends FinanceProduct {

    @Min(1)
    private BigDecimal valueLimit;
    @Future
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate validityDate;
    @Column(precision = 5, scale=4)
    @DecimalMin("0.01")
    @DecimalMax("100")
    private BigDecimal interestAboveLimit;
}
