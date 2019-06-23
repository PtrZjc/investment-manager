package pl.zajacp.investmentmanager.products;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import pl.zajacp.investmentmanager.products.validation.LessInterestAboveLimit;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@LessInterestAboveLimit
@DiscriminatorValue("savings account")
public class SavingsAccount extends FinanceProduct {

    @Min(1)
    private BigDecimal valueLimit;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate validityDate;

    @NotNull
    @Column(precision = 5, scale=4)
    @DecimalMin("0.001")
    @DecimalMax("1")
    private BigDecimal interestAboveLimit;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if(valueLimit==null){
            valueLimit=BigDecimal.valueOf(Integer.MAX_VALUE);
        };
    }

    @Override
    public String toString() {
        return "SavingsAccount{" +
                "valueLimit=" + valueLimit +
                ", validityDate=" + validityDate +
                ", interestAboveLimit=" + interestAboveLimit +
                "} " + super.toString();
    }
}
