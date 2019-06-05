package pl.zajacp.investmentmanager.products;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.actionmanagement.FinanceCalcService;
import pl.zajacp.investmentmanager.charts.ChartService;
import pl.zajacp.investmentmanager.products.investment.Investment;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;
import pl.zajacp.investmentmanager.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final UserService userService;
    private final ActionService actionService;
    private final ProductRepository productRepository;
    private final ChartService chartService;
    private final FinanceCalcService financeCalcService;

    @Autowired
    public ProductService(ProductRepository productRepository, UserService userService,
                          ActionService actionService, ChartService chartService, FinanceCalcService financeCalcService) {
        this.productRepository = productRepository;
        this.userService = userService;
        this.actionService = actionService;
        this.chartService = chartService;
        this.financeCalcService = financeCalcService;
    }

    public void save(FinanceProduct product) {
        product.setUser(userService.getLoggedUser());
        productRepository.save(product);

        Hibernate.initialize(product.getActions());

        if (product.getActions().size() == 0) {
            if (product instanceof Investment) {
                actionService.initializeInvestmentActions((Investment) product);
            } else if (product instanceof SavingsAccount) {
                actionService.initializeSavingsAccountActions((SavingsAccount) product);
            }
        }
    }

    public FinanceProduct findById(Long id) {
        return productRepository.findById(id).orElseThrow(NullPointerException::new);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public Map<Class, List<FinanceProduct>> findAllOfLoggedUser() {
        return productRepository.findAllByUserOrderByCreatedDesc(userService.getLoggedUser()).stream()
                .collect(Collectors.groupingBy(FinanceProduct::getClass));
    }

    public void sortActionsByDate(FinanceProduct product) {
        List<Action> actions = product.getActions();
        actions.sort(Comparator
                .comparing(Action::getActionDate)
                .thenComparing(a -> a.getActionType().toString()));

        product.setActions(actions);
    }

    public void getAdditionalSavingsAccountViewData(SavingsAccount product, Model model) {
        List<Action> actions = actionService.getChartActions((SavingsAccount) product);
        Map<LocalDate, BigDecimal> gain = financeCalcService.getGain(actions);

        String valueChartData = chartService.getValuePlot((SavingsAccount) product, actions);
        String gainChartData = chartService.getGainPlot((SavingsAccount) product, gain, actions);

        model.addAttribute("valueData", valueChartData);
        model.addAttribute("gainData", gainChartData);
    }

}
