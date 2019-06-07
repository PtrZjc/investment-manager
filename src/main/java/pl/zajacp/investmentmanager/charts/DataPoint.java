package pl.zajacp.investmentmanager.charts;

import lombok.Getter;
import lombok.Setter;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

public class DataPoint implements Cloneable {

    @Getter
    @Setter
    long t;

    @Getter
    @Setter
    BigDecimal y;

    @Getter
    @Setter
    String action;

    DataPoint(){};

    DataPoint(LocalDate date, ActionType actionType) {
        /*
         * Epoch shift avoids overlapping of capitalization and balance change datapoints on the chart.
         * Balance change is shown at 12:00 PM, while capitalization at 11:59 PM.
         * */
        this.t = ActionType.BALANCE_CHANGE.equals(actionType) ?
                (date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() + (12 * 60 * 60)) * 1000 :
                (date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() + (24 * 60 * 60) - 1) * 1000;
    }

    public DataPoint copy() {
        DataPoint copied = new DataPoint();
        copied.setAction(this.action);
        copied.setY(this.y);
        copied.setT(this.t);
        return copied;
    }
}

