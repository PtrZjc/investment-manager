package pl.zajacp.investmentmanager.charts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartService {

    public ChartService() {
    }

    ;

    public String jsonMapper(Collection objectToJson) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(objectToJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public List<DataPoint> initializeValueData(List<Action> actions) {
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

    public List<DataPoint> initializeGainData(Map<LocalDate, BigDecimal> gains, LocalDate startDate) {

        List<Map.Entry<LocalDate, BigDecimal>> futureGains = gains.entrySet().stream()
                .filter(e -> e.getKey().isAfter(startDate))
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

    public void equalizeSummaryChartPlots(List<SummaryChartDTO> charts) {
        long maxValueTime = charts.stream()
                .flatMap(chart -> chart.getValuePlot().stream())
                .map(DataPoint::getT)
                .max(Long::compareTo)
                .orElseThrow(NullPointerException::new);

        for (SummaryChartDTO chartData : charts) {
            List<DataPoint> valuePlot = chartData.getValuePlot();
            int lastIndex = valuePlot.size() - 1;
            if (valuePlot.get(lastIndex).getT() == maxValueTime) {
                continue;
            }

            DataPoint dataPoint = new DataPoint();
            dataPoint.setT(maxValueTime);
            dataPoint.setY(valuePlot.get(lastIndex).getY());
            valuePlot.add(dataPoint);
        }
    }

    public long getMaxCommonTime(List<SummaryChartDTO> charts) {
        List<Long> maxTimes = new ArrayList<>();
        for (SummaryChartDTO chartData : charts) {
            List<DataPoint> valuePlot = chartData.getValuePlot();
            int lastIndex = valuePlot.size() - 1;
            maxTimes.add(valuePlot.get(lastIndex).getT());
        }
        return maxTimes.stream()
                .min(Long::compareTo)
                .orElseThrow(NullPointerException::new);
    }
}
