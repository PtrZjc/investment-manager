package pl.zajacp.investmentmanager.products.savings;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.validation.InterestAboveLimitPresent;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@InterestAboveLimitPresent
@DiscriminatorValue("savings account")
public class SavingsAccount extends FinanceProduct {

    @Min(1)
    @NotNull
    private BigDecimal valueLimit;

    @Future
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate validityDate;

    @Column(precision = 5, scale=4)
    @DecimalMin("0.00")
    @DecimalMax("100")
    private BigDecimal interestAboveLimit;

}
