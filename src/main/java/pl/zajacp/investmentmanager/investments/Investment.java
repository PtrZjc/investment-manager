package pl.zajacp.investmentmanager.investments;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("investment")
public class Investment extends FinanceProduct {

}
