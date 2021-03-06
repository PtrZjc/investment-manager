package pl.zajacp.investmentmanager.products;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.data.ChartService;
import pl.zajacp.investmentmanager.data.DataPoint;
import pl.zajacp.investmentmanager.data.FinanceCalcService;
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

        if (product.getActions().isEmpty()) {
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

    public String getValuePlotData(SavingsAccount product) {
        List<Action> actions = product.getActions();
        List<DataPoint> valuePlot = chartService.initializeValueData(actions);
        return chartService.jsonMapper(valuePlot);
    }

    public String getGainPlotData(SavingsAccount product){
        List<Action> actions = product.getActions();
        LocalDate startDate = actions.get(0).getActionDate().minusMonths(1);
        Map<LocalDate, BigDecimal> gain = financeCalcService.getGain(actions);
        List<DataPoint> gainPlot = chartService.initializeGainData(gain,startDate);
        return chartService.jsonMapper(gainPlot);
    }
}
