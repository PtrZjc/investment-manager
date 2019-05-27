package pl.zajacp.investmentmanager.products;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.products.investment.Investment;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;
import pl.zajacp.investmentmanager.user.UserService;

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

    @Autowired
    public ProductService(ProductRepository productRepository, UserService userService, ActionService actionService) {
        this.productRepository = productRepository;
        this.userService = userService;
        this.actionService = actionService;
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

    public void sortActionsByDate(FinanceProduct product, boolean reverse) {
        List<Action> actions = product.getActions();
         actions.sort(Comparator
                 .comparing(Action::getActionDate)
                 .thenComparing(a->a.getActionType().toString()));

        product.setActions(actions);
    }
}
