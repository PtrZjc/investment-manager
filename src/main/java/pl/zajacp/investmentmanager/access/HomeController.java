package pl.zajacp.investmentmanager.access;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.zajacp.investmentmanager.charts.ChartService;
import pl.zajacp.investmentmanager.charts.SummaryChartDTO;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.user.User;
import pl.zajacp.investmentmanager.user.UserService;

import java.util.List;

@Controller
public class HomeController {

    private final ChartService chartService;
    private final UserService userService;

    public HomeController(ChartService chartService, UserService userService) {
        this.chartService = chartService;
        this.userService = userService;
    }

    @GetMapping("/home")
    public String index(Model model) {

        User activeUser = userService.getLoggedUser();
        List<FinanceProduct> products = activeUser.getProducts();

        if(products.size()>0) {
            List<SummaryChartDTO> chartData = chartService.initializeSummaryChartData(products);
            chartService.equalizeSummaryGainPlots(chartData);
            chartService.equalizeSummaryValuePlots(chartData);
            model.addAttribute("maxTime", chartService.getMaxDataPointTime(chartData));
            model.addAttribute("maxSharedTime", chartService.getMaxDataPointTime(chartData, true));
            model.addAttribute("data", chartService.jsonMapper(chartData));
        }

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/accessDenied")
    public String error403() {
        User user = null;
        return "403";
    }

    @GetMapping("/logged")
    public String loggedInfo() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
        } else {
            String username = principal.toString();
        }

        System.out.println(principal);

        return "index";
    }

}
