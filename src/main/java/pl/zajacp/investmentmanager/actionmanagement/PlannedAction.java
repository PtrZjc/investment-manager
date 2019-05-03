package pl.zajacp.investmentmanager.actionmanagement;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("planned")
public class PlannedAction extends Action{

}
