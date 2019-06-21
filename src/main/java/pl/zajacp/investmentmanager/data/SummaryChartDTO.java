package pl.zajacp.investmentmanager.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SummaryChartDTO {

    private String productName;
    private String productType;
    private List<DataPoint> valuePlot;
    private List<DataPoint> gainPlot;
}
