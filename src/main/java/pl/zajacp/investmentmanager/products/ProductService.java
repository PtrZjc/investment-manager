package pl.zajacp.investmentmanager.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.generics.AbstractCrudService;
import pl.zajacp.investmentmanager.products.investment.Investment;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;
import pl.zajacp.investmentmanager.user.UserService;

@Service
@Transactional
public class ProductService extends AbstractCrudService<FinanceProduct> {

    private final UserService userService;
    private final ActionService actionService;

    @Autowired
    public ProductService(ProductRepository repo, UserService userService, ActionService actionService) {
        this.repo = repo;
        this.userService = userService;
        this.actionService = actionService;
    }

    @Override
    public void save(FinanceProduct product) {

        product.setUser(userService.getLoggedUser());
        repo.save(product);

        if (product instanceof Investment){
            actionService.initializeInvestmentActions((Investment) product);
        }else{
            actionService.initializeSavingsAccountActions((SavingsAccount) product);
        }
    }
}
