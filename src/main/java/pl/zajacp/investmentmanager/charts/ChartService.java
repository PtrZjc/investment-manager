package pl.zajacp.investmentmanager.charts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartService {

    public ChartService(){};

    public String getValuePlot(SavingsAccount product, List<Action> actions) {

        ObjectMapper mapper = new ObjectMapper();
        String totalValueData = null;
        try {
            totalValueData = mapper.writeValueAsString(initializeValueData(actions));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return totalValueData;
    }

    private List<DataPoint> initializeValueData(List<Action> actions) {
        List<DataPoint> data = new ArrayList<>();
        BigDecimal currentValue = actions.get(0).getAfterActionValue();
        Locale locale = LocaleContextHolder.getLocale();
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);

        for (Action action : actions) {

            DataPoint point = new DataPoint(action.getActionDate(), action.getActionType());

            switch (action.getActionType()) {
                case CAPITALIZATION:
                    currentValue = action.getAfterActionValue();
                    point.setAction(messages.getString("product.charts.action.capitalization"));
                    break;
                case BALANCE_CHANGE:
                    currentValue = currentValue.add(action.getBalanceChange());
                    if (action.getBalanceChange().compareTo(BigDecimal.ZERO) > 0) {
                        point.setAction(messages.getString("product.charts.action.payment"));
                    } else {
                        point.setAction(messages.getString("product.charts.action.widthdraw"));
                    }
                    break;
                case PRODUCT_CLOSE:
                    point.setAction(messages.getString("product.charts.action.endPromotion"));
                    break;
                case PRODUCT_OPEN:
                    point.setAction(messages.getString("product.charts.action.productOpen"));
                    break;
            }
            point.setY(currentValue);
            data.add(point);
        }
        return data;
    }

    public String getGainPlot(SavingsAccount product, Map<LocalDate, BigDecimal> gain, List<Action> actions ) {

        ObjectMapper mapper = new ObjectMapper();
        String gainData = null;
        try {
            gainData = mapper.writeValueAsString(initializeGainData(gain, actions));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return gainData;
    }

    private List<DataPoint> initializeGainData(Map<LocalDate, BigDecimal> gains, List<Action> actions) {
        LocalDate startPoint = actions.get(0).getActionDate();

        LocalDate date = actions.get(0).getActionDate();
        List<Map.Entry<LocalDate, BigDecimal>> futureGains = gains.entrySet().stream()
                .filter(e -> e.getKey().isAfter(date))
                .collect(Collectors.toList());

        List<DataPoint> data = new ArrayList<>();
        Locale locale = LocaleContextHolder.getLocale();
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);

        for (Map.Entry<LocalDate, BigDecimal> gain : futureGains) {
            DataPoint point = new DataPoint(gain.getKey(), ActionType.GAIN);
            point.setAction(messages.getString("product.charts.action.gain"));
            point.setY(gain.getValue());
            data.add(point);
        }
        return data;
    }

}
