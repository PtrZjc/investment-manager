package pl.zajacp.investmentmanager.charts;

import org.springframework.stereotype.Service;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;
import pl.zajacp.investmentmanager.actionmanagement.FinanceCalcService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChartService {

    private final ActionService actionService;
    private final FinanceCalcService financeCalcService;

    public ChartService(ActionService actionService, FinanceCalcService financeCalcService) {
        this.actionService = actionService;
        this.financeCalcService = financeCalcService;
    }

    public List<DataPoint> initializeValueData(List<Action> actions) {
        List<DataPoint> data = new ArrayList<>();
        BigDecimal currentValue = actions.get(0).getAfterActionValue();

        for (Action action : actions) {
            DataPoint point = new DataPoint(action.getActionDate(), action.getActionType());

            if (action.getActionType() == ActionType.CAPITALIZATION) {
                currentValue = action.getAfterActionValue();
                point.setAction("[[#{product.charts.action.capitalization }]]");
            } else if (action.getActionType() == ActionType.BALANCE_CHANGE) {
                currentValue = currentValue.add(action.getBalanceChange());
                if (action.getBalanceChange().compareTo(BigDecimal.ZERO) > 0) {
                    point.setAction("[[#{product.charts.action.payment}]]");
                } else {
                    point.setAction("[[#{product.charts.action.widthdraw}]]");
                }
            } else if (action.getActionType() == ActionType.PRODUCT_CLOSE) {
                point.setAction("[[#{product.charts.action.endPromotion}]]");
            }
            point.setX(currentValue);
            data.add(point);
        }

        return data;
    }
}
