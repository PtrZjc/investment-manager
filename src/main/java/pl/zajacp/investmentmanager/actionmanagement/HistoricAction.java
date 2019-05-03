package pl.zajacp.investmentmanager.actionmanagement;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("historic")
public class HistoricAction extends Action{

}
