package pl.zajacp.investmentmanager.products.investment;

import lombok.Getter;
import lombok.Setter;
import pl.zajacp.investmentmanager.products.FinanceProduct;

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

}

