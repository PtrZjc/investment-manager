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
import java.util.HashMap;
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

    public Map<Class, List<? extends FinanceProduct>> findAllOfLoggedUser() {

        List<FinanceProduct> allProducts = productRepository.findAllByUserOrderByCreatedDesc(userService.getLoggedUser());
        Map<Class, List<? extends FinanceProduct>> separatedProducts = new HashMap<>();

        List<Investment> investments = allProducts.stream()
                .filter(x -> x instanceof Investment)
                .map(x -> (Investment) x)
                .collect(Collectors.toList());
        List<SavingsAccount> savingsAccounts = allProducts.stream()
                .filter(x -> x instanceof SavingsAccount)
                .map(x -> (SavingsAccount) x)
                .collect(Collectors.toList());

        separatedProducts.put(Investment.class, investments);
        separatedProducts.put(SavingsAccount.class, savingsAccounts);

        return separatedProducts;
    }

    public void sortActionsByDate(FinanceProduct product, boolean reverse) {
        Comparator<Action> comp = (a1, a2) -> {
            int compare = a1.getActionDate().compareTo(a2.getActionDate());
            if (compare == 0) {
                compare = a1.getActionType().toString().compareTo(a2.getActionType().toString());
            }
            return compare;
        };
        List<Action> actions = product.getActions();
        if (reverse) {
            actions.sort(comp);
        } else {
            actions.sort(comp.reversed());
        }
        product.setActions(actions);
    }
}
