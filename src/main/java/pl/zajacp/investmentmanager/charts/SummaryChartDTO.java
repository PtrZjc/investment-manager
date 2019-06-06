package pl.zajacp.investmentmanager.charts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SummaryChartDTO {

    private String productName;
    private List<DataPoint> valuePlot;
    private List<DataPoint> gainPlot;
}
