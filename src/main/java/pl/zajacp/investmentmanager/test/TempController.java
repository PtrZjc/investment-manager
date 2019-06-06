package pl.zajacp.investmentmanager.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionRepository;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.actionmanagement.FinanceCalcService;
import pl.zajacp.investmentmanager.charts.ChartService;
import pl.zajacp.investmentmanager.charts.DataPoint;
import pl.zajacp.investmentmanager.charts.SummaryChartDTO;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.ProductRepository;
import pl.zajacp.investmentmanager.products.ProductService;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;
import pl.zajacp.investmentmanager.user.User;
import pl.zajacp.investmentmanager.user.UserService;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class TempController {
//temporary class for development only

    @Autowired
    private UserService userService;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ChartService chartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private FinanceCalcService financeCalcService;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    EntityManager entityManager;

    @GetMapping("/data")
    public String home(Model model) {
        User user = userService.getLoggedUser();

        List<FinanceProduct> products = user.getProducts();

        List<List<Action>> chartActions = products.stream()
                .filter(product -> product instanceof SavingsAccount)
                .map(savingsAccount -> actionService.getChartActions((SavingsAccount) savingsAccount))
                .collect(Collectors.toList());

        chartActions.forEach(actionList -> actionService.sortActionsByDate(actionList));

        List<Map<LocalDate, BigDecimal>> productGains = chartActions.stream()
                .map(actions -> financeCalcService.getGain(actions))
                .collect(Collectors.toList());

        List<SummaryChartDTO> chartData = new ArrayList<>();

        for (int i = 0; i < chartActions.size(); i++) {
            LocalDate startDate = chartActions.get(i).get(0).getActionDate().minusMonths(1);
            String productName = chartActions.get(i).get(0).getProduct().getBank();
            List<DataPoint> valuePlot = chartService.initializeValueData(chartActions.get(i));
            List<DataPoint> gainPlot = chartService.initializeGainData(productGains.get(i), startDate);

            SummaryChartDTO dataset = new SummaryChartDTO();
            dataset.setProductName(productName);
            dataset.setValuePlot(valuePlot);
            dataset.setGainPlot(gainPlot);
            chartData.add(dataset);
        }

        model.addAttribute("maxCommonTime", chartService.getMaxCommonTime(chartData));
        model.addAttribute("data", chartService.jsonMapper(chartData));
        return "index";
    }

//    @GetMapping("/data2")
//    @ResponseBody
//    public String home2(Model model) {
//        User user = userService.getLoggedUser();
//
//        List<FinanceProduct> products = user.getProducts();
//
//        List<List<Action>> chartActions = products.stream()
//                .filter(product -> product instanceof SavingsAccount)
//                .map(savingsAccount -> actionService.getChartActions((SavingsAccount) savingsAccount))
//                .collect(Collectors.toList());
//
//        List<Map<LocalDate, BigDecimal>> productGains = chartActions.stream()
//                .map(actions -> financeCalcService.getGain(actions))
//                .collect(Collectors.toList());
//
//        List<SummaryChartDTO> chartData = new ArrayList<>();
//
//
//        String jsonValuePlots = null;
//        String jsonGainPlots = null;
//
//        for (int i = 0; i < chartActions.size(); i++) {
//            LocalDate startDate = chartActions.get(i).get(0).getActionDate().minusMonths(1);
//            String productName = chartActions.get(i).get(0).getProduct().getBank();
//            List<DataPoint> valuePlot = chartService.initializeValueData(chartActions.get(i));
//            List<DataPoint> gainPlot = chartService.initializeGainData(productGains.get(i), startDate);
//
//            SummaryChartDTO dataset = new SummaryChartDTO();
//            dataset.setProductName(productName);
//            dataset.setValuePlot(valuePlot);
//            dataset.setGainPlot(gainPlot);
//            chartData.add(dataset);
//        }
//
//        return chartService.jsonMapper(chartData);
//    }
}
