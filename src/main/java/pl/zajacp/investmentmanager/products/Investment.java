package pl.zajacp.investmentmanager.products;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@DiscriminatorValue("investment")
public class Investment extends FinanceProduct {

    @NotNull
    private Long monthsValid;

    @Override
    public String toString() {
        return "Investment{" +
                "monthsValid=" + monthsValid +
                "} " + super.toString();
    }
}

