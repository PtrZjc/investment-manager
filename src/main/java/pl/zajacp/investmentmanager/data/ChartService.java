package pl.zajacp.investmentmanager.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.Investment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartService {

    private enum PlotType {
        GAIN, VALUE;
    }

    private final ActionService actionService;
    private final FinanceCalcService financeCalcService;

    public ChartService(ActionService actionService, FinanceCalcService financeCalcService) {
        this.actionService = actionService;
        this.financeCalcService = financeCalcService;
    }

    public List<List<Action>> getInitialChartActions(List<FinanceProduct> products) {
        List<List<Action>> chartActions = products.stream()
                .filter(FinanceProduct::getIsActive)
                .map(FinanceProduct::getActions)
                .collect(Collectors.toList());

        chartActions.forEach(actionService::sortActionsByDate);
        return chartActions;
    }

    public List<Map<LocalDate, BigDecimal>> getInitialProductGains(List<List<Action>> chartActions) {
        return chartActions.stream()
                .map(financeCalcService::getGain)
                .collect(Collectors.toList());
    }

    public List<SummaryChartDTO> initializeSummaryChartData(List<List<Action>> chartActions,
                                                            List<Map<LocalDate, BigDecimal>> productGains) {
                List<SummaryChartDTO> chartData = new ArrayList<>();

        for (int i = 0; i < chartActions.size(); i++) {
            LocalDate startDate = chartActions.get(i).get(0).getActionDate().minusMonths(1);
            String productName = chartActions.get(i).get(0).getProduct().getBank();
            String productType = chartActions.get(i).get(0).getProduct().getClass().toString().split("\\.")[4];
            List<DataPoint> valuePlot = initializeValueData(chartActions.get(i));
            List<DataPoint> gainPlot = initializeGainData(productGains.get(i), startDate);

            SummaryChartDTO dataset = new SummaryChartDTO();
            dataset.setProductName(productName);
            dataset.setProductType(productType);
            dataset.setValuePlot(valuePlot);
            dataset.setGainPlot(gainPlot);
            chartData.add(dataset);
        }
        addInitialZeroGainToOldestGainPlot(chartData);
        equalizeSummaryPlots(chartData);
        ensureProperOrderOnChart(chartData);

        return chartData;
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
                    if (action.getProduct() instanceof Investment) {
                        currentValue = action.getAfterActionValue();
                        point.setAction(setLocaleLabel("product.investmentEnd"));
                    } else {
                        point.setAction(setLocaleLabel("product.productClose"));
                    }
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

    private void equalizeSummaryPlots(List<SummaryChartDTO> charts) {

        List<Long> uniqueValueTimePoints = charts.stream()
                .map(SummaryChartDTO::getValuePlot)
                .flatMap(Collection::stream)
                .map(DataPoint::getT)
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));

        List<Long> uniqueGainTimePoints = charts.stream()
                .map(SummaryChartDTO::getGainPlot)
                .flatMap(Collection::stream)
                .map(DataPoint::getT)
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));

        for (SummaryChartDTO chart : charts) {
            fillChartWithSharedDataPoints(chart, uniqueValueTimePoints, PlotType.VALUE);
            fillChartWithSharedDataPoints(chart, uniqueGainTimePoints, PlotType.GAIN);
        }
    }

    private void fillChartWithSharedDataPoints(SummaryChartDTO chart, List<Long> allUniqueTimePoints, PlotType plotType) {

        List<DataPoint> plot = null;
        if (plotType == PlotType.GAIN) {
            plot = chart.getGainPlot();
        } else if (plotType == PlotType.VALUE) {
            plot = chart.getValuePlot();
        }

        List<DataPoint> plotWithSharedPoints = new ArrayList<>(allUniqueTimePoints.size());
        boolean isInvestment = "Investment".equals(chart.getProductType());

        if (isInvestment && plotType == PlotType.VALUE) {
            plot.add(plot.get(1).copy());
        }

        int currentPointIndex = 0;
        BigDecimal currentValue = BigDecimal.ZERO;
        DataPoint currentPoint = plot.get(0);

        for (Long dataPointTime : allUniqueTimePoints) {
            if (currentPoint.getT() == dataPointTime &&
                    (currentPointIndex < plot.size() - 1 || isInvestment)) {
                currentValue = currentPoint.getY();
                if (plotType == PlotType.GAIN && !isInvestment) {
                    currentPoint = plot.get(++currentPointIndex);
                } else if (plotType == PlotType.VALUE) {
                    currentPoint = plot.get(++currentPointIndex);
                }
            }
            DataPoint point = new DataPoint();
            point.setT(dataPointTime);
            point.setY(currentValue);
            plotWithSharedPoints.add(point);
        }

        if (plotType == PlotType.GAIN) {
            chart.setGainPlot(plotWithSharedPoints);
        } else if (plotType == PlotType.VALUE) {
            chart.setValuePlot(plotWithSharedPoints);
        }
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

    private void addInitialZeroGainToOldestGainPlot(List<SummaryChartDTO> charts) {

        List<DataPoint> oldestGainPlot = charts.stream()
                .map(SummaryChartDTO::getGainPlot)
                .filter(p -> p.size() > 1)
                .min((p1, p2) -> (int) (p1.get(0).getT() - p2.get(0).getT()))
                .orElseThrow(NullPointerException::new);

        LocalDate firstGainDate = LocalDate.ofEpochDay(oldestGainPlot.get(0).getT() / 86400000);
        LocalDate monthBeforeFirstGain = firstGainDate.minusMonths(1);
        DataPoint initialZeroGain = new DataPoint(monthBeforeFirstGain, ActionType.GAIN);
        initialZeroGain.setY(BigDecimal.ZERO);
        initialZeroGain.setAction(setLocaleLabel("product.gain"));
        oldestGainPlot.add(0, initialZeroGain);

    }

    public long getMaxDataPointTime(List<SummaryChartDTO> charts) {
        return getMaxDataPointTime(charts, false);
    }

    public long getMaxDataPointTime(List<SummaryChartDTO> charts, boolean sharedTime) {
        List<Long> maxTimes = new ArrayList<>();
        for (SummaryChartDTO chartData : charts) {
            List<DataPoint> valuePlot = chartData.getValuePlot();
            int lastIndex = valuePlot.size() - 1;
            maxTimes.add(valuePlot.get(lastIndex).getT());
        }
        if (sharedTime) {
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

    private void ensureProperOrderOnChart(List<SummaryChartDTO> chartData) {
        int midValueIndex = chartData.get(0).getValuePlot().size() / 2;
        Comparator<SummaryChartDTO> compareMidValue = Comparator.comparing
                (ch -> ch.getValuePlot().get(midValueIndex).getY(), Comparator.reverseOrder());

        chartData.sort(Comparator
                .comparing(SummaryChartDTO::getProductType)
                .thenComparing(compareMidValue));
    }
}
