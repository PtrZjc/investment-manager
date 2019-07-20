package pl.zajacp.investmentmanager.access;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.data.ChartService;
import pl.zajacp.investmentmanager.data.StatisticsService;
import pl.zajacp.investmentmanager.data.SummaryChartDTO;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.user.User;
import pl.zajacp.investmentmanager.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final ChartService chartService;
    private final UserService userService;
    private final StatisticsService statisticsService;

    public HomeController(ChartService chartService, UserService userService, StatisticsService statisticsService) {
        this.chartService = chartService;
        this.userService = userService;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/home")
    public String index(Model model) {

        User activeUser = userService.getLoggedUser();
        List<FinanceProduct> products = activeUser.getProducts();

        if (!products.isEmpty()) {
            model.addAttribute("products", true);

            List<List<Action>> chartActions = chartService.getInitialChartActions(products);
            List<Map<LocalDate, BigDecimal>> gains = chartService.getInitialProductGains(chartActions);
            List<SummaryChartDTO> chartData = chartService.initializeSummaryChartData(chartActions, gains);

            model.addAttribute("maxTime", chartService.getMaxDataPointTime(chartData));
            model.addAttribute("maxSharedTime", chartService.getMaxDataPointTime(chartData, true));
            model.addAttribute("data", chartService.jsonMapper(chartData));

            BigDecimal currentValue = statisticsService.getTotalUserValueInMonth(activeUser, 0);
            BigDecimal gainOneMonthAgo = statisticsService.getUserGainInMonth(gains, 1);
            BigDecimal gainThreeMonthsAgo = statisticsService.getUserGainInMonth(gains, 3);
            BigDecimal gainTwelveMonthsAgo = statisticsService.getUserGainInMonth(gains, 12);

            model.addAttribute("currentValue", currentValue);
            model.addAttribute("gainOneMonthAgo", gainOneMonthAgo);
            model.addAttribute("gainThreeMonthsAgo", gainThreeMonthsAgo);
            model.addAttribute("gainTwelveMonthsAgo", gainTwelveMonthsAgo);
        } else {
            model.addAttribute("products", false);
        }
        model.addAttribute("userName", activeUser.getName());

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/accessDenied")
    public String error403() {
        return "403";
    }
}
