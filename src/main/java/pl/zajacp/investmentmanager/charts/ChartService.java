package pl.zajacp.investmentmanager.charts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;
import pl.zajacp.investmentmanager.actionmanagement.FinanceCalcService;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.SavingsAccount;
import pl.zajacp.investmentmanager.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartService {

    private final ActionService actionService;
    private final FinanceCalcService financeCalcService;

    public ChartService(ActionService actionService, FinanceCalcService financeCalcService) {
        this.actionService = actionService;
        this.financeCalcService = financeCalcService;
    }

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

        for (Action action : actions) {

            DataPoint point = new DataPoint(action.getActionDate(), action.getActionType());

            switch (action.getActionType()) {
                case CAPITALIZATION:
                    currentValue = action.getAfterActionValue();
                    point.setAction(setLocaleLabel("product.capitalization"));
                    break;
                case BALANCE_CHANGE:
                    currentValue = currentValue.add(action.getBalanceChange());
                    if (action.getBalanceChange().compareTo(BigDecimal.ZERO) > 0) {
                        point.setAction(setLocaleLabel("product.payment"));
                    } else {
                        point.setAction(setLocaleLabel("product.widthdraw"));
                    }
                    break;
                case PRODUCT_CLOSE:
                    point.setAction(setLocaleLabel("product.endPromotion"));
                    break;
                case PRODUCT_OPEN:
                    point.setAction(setLocaleLabel("product.productOpen"));
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


        for (Map.Entry<LocalDate, BigDecimal> gain : futureGains) {
            DataPoint point = new DataPoint(gain.getKey(), ActionType.GAIN);
            point.setAction(setLocaleLabel("product.gain"));
            point.setY(gain.getValue());
            data.add(point);
        }
        return data;
    }

    public List<SummaryChartDTO> initializeSummaryChartData(User user) {

        List<FinanceProduct> products = user.getProducts();
        List<List<Action>> chartActions = products.stream()
                .filter(product -> product instanceof SavingsAccount)
                .map(FinanceProduct::getActions)
                .collect(Collectors.toList());

        chartActions.forEach(actionService::sortActionsByDate);

        List<Map<LocalDate, BigDecimal>> productGains = chartActions.stream()
                .map(financeCalcService::getGain)
                .collect(Collectors.toList());

        List<SummaryChartDTO> chartData = new ArrayList<>();

        for (int i = 0; i < chartActions.size(); i++) {
            LocalDate startDate = chartActions.get(i).get(0).getActionDate().minusMonths(1);
            String productName = chartActions.get(i).get(0).getProduct().getBank();
            List<DataPoint> valuePlot = initializeValueData(chartActions.get(i));
            List<DataPoint> gainPlot = initializeGainData(productGains.get(i), startDate);

            SummaryChartDTO dataset = new SummaryChartDTO();
            dataset.setProductName(productName);
            dataset.setValuePlot(valuePlot);
            dataset.setGainPlot(gainPlot);
            chartData.add(dataset);
        }
        return chartData;
    }

    public void equalizeSummaryGainPlots(List<SummaryChartDTO> charts) {

        SummaryChartDTO oldestChart = charts.stream()
                .min((ch1, ch2) -> (int) (ch1.getGainPlot().get(0).getT() - ch2.getGainPlot().get(0).getT()))
                .orElseThrow(NullPointerException::new);

        long latestDataPointTime = charts.stream()
                .map(ch -> ch.getGainPlot().get(ch.getGainPlot().size() - 1).getT())
                .max(Long::compareTo)
                .orElseThrow(NullPointerException::new);

        List<DataPoint> oldestGainPlot = oldestChart.getGainPlot();
        addInitialZeroGain(oldestGainPlot);

        for (SummaryChartDTO chartData : charts) {
            List<DataPoint> gainPlot = chartData.getGainPlot();
            if (gainPlot == oldestGainPlot) {
                continue;
            }

            List<DataPoint> dataWithTrailingZeros = addLeadingZeroGainLikeOldest(gainPlot, oldestGainPlot);
            dataWithTrailingZeros.addAll(gainPlot);
            chartData.setGainPlot(dataWithTrailingZeros);
        }
        fillTrailingConstantGainToLatest(charts);
    }

    private void addInitialZeroGain(List<DataPoint> gainPlot) {
        LocalDate firstGainDate = LocalDate.ofEpochDay(gainPlot.get(0).getT() / 86400000);
        LocalDate monthBeforeFirstGain = firstGainDate.minusMonths(1);
        DataPoint initialZeroGain = new DataPoint(monthBeforeFirstGain, ActionType.GAIN);
        initialZeroGain.setY(BigDecimal.ZERO);
        initialZeroGain.setAction(setLocaleLabel("product.gain"));
        gainPlot.add(0, initialZeroGain);
    }

    private List<DataPoint> addLeadingZeroGainLikeOldest(List<DataPoint> gainPlot, List<DataPoint> oldestGainPlot) {
        List<DataPoint> dataWithTrailingZeros = new ArrayList<>();
        int i = 0;
        while (gainPlot.get(0).getT() != oldestGainPlot.get(i).getT()) {
            DataPoint dataPoint = new DataPoint();
            dataPoint.setY(BigDecimal.ZERO);
            dataPoint.setT(oldestGainPlot.get(i++).getT());
            dataPoint.setAction(setLocaleLabel("product.gain"));
            dataWithTrailingZeros.add(dataPoint);
        }
        return dataWithTrailingZeros;
    }

    private void fillTrailingConstantGainToLatest(List<SummaryChartDTO> charts) {
        Comparator<SummaryChartDTO> lastDataPointTimeComparator =
                (ch1, ch2) -> (int) (ch1.getGainPlot().get(ch1.getGainPlot().size() - 1).getT()
                        - ch2.getGainPlot().get(ch2.getGainPlot().size() - 1).getT());

        List<DataPoint> latestGainPlot = charts.stream()
                .max(lastDataPointTimeComparator)
                .map(SummaryChartDTO::getGainPlot)
                .orElseThrow(NullPointerException::new);

        for (SummaryChartDTO chart : charts) {
            List<DataPoint> gainPlot = chart.getGainPlot();

            int index = gainPlot.size() - 1;

            long latestDataPointTime = latestGainPlot.get(latestGainPlot.size() - 1).getT();
            while (gainPlot.get(index).getT() != latestDataPointTime) {
                DataPoint dataPoint = new DataPoint();
                dataPoint.setY(gainPlot.get(index).getY());
                dataPoint.setT(latestGainPlot.get(++index).getT());
                dataPoint.setAction(setLocaleLabel("product.gain"));
                gainPlot.add(dataPoint);
            }
        }
    }

    public long getMaxDataPointTime(List<SummaryChartDTO> charts){
        return getMaxDataPointTime(charts,false);
    }

    public long getMaxDataPointTime(List<SummaryChartDTO> charts,boolean sharedTime) {
        List<Long> maxTimes = new ArrayList<>();
        for (SummaryChartDTO chartData : charts) {
            List<DataPoint> valuePlot = chartData.getValuePlot();
            int lastIndex = valuePlot.size() - 1;
            maxTimes.add(valuePlot.get(lastIndex).getT());
        }
        if(sharedTime){
            return maxTimes.stream()
                    .min(Long::compareTo)
                    .orElseThrow(NullPointerException::new);
        }
        return maxTimes.stream()
                .max(Long::compareTo)
                .orElseThrow(NullPointerException::new);
    }


    private String setLocaleLabel(String messageKey) {
        Locale locale = LocaleContextHolder.getLocale();
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        return messages.getString(messageKey);
    }
}
