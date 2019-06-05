package pl.zajacp.investmentmanager.charts;

import lombok.Getter;
import lombok.Setter;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

public class DataPoint {

    @Getter
    @Setter
    long t;

    @Getter
    @Setter
    BigDecimal y;

    @Getter
    @Setter
    String action;

    public DataPoint(LocalDate date, ActionType actionType) {
        /*
         * Epoch shift avoids overlapping of capitalization and balance change datapoints on the chart.
         * Balance change is shown at 12:00 PM, while capitalization at 11:59 PM.
         * */
        this.t = actionType == ActionType.BALANCE_CHANGE ?
                (date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() + (12 * 60 * 60)) * 1000:
                (date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() + (24 * 60 * 60)-1) * 1000;
    }
}
