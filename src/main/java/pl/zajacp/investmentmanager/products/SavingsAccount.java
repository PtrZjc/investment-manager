package pl.zajacp.investmentmanager.products;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import pl.zajacp.investmentmanager.products.validation.InterestAboveLimitPresent;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
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
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal interestAboveLimit;

    @Override
    public String toString() {
        return "SavingsAccount{" +
                "valueLimit=" + valueLimit +
                ", validityDate=" + validityDate +
                ", interestAboveLimit=" + interestAboveLimit +
                "} " + super.toString();
    }
}