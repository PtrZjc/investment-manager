package pl.zajacp.investmentmanager.products;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.data.FinanceCalcService;
import pl.zajacp.investmentmanager.data.ChartService;
import pl.zajacp.investmentmanager.data.DataPoint;
import pl.zajacp.investmentmanager.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    public void getAdditionalSavingsAccountViewData(SavingsAccount product, Model model) {
        List<Action> actions = product.getActions();

        LocalDate startDate = actions.get(0).getActionDate().minusMonths(1);
        Map<LocalDate, BigDecimal> gain = financeCalcService.getGain(actions);
        List<DataPoint> gainPlot = chartService.initializeGainData(gain,startDate);
        String jsonGainPlot = chartService.jsonMapper(gainPlot);
        model.addAttribute("gainData", jsonGainPlot);

        List<DataPoint> valuePlot = chartService.initializeValueData(actions);
        String jsonValuePlot = chartService.jsonMapper(valuePlot);
        model.addAttribute("valueData", jsonValuePlot);
    }
}
