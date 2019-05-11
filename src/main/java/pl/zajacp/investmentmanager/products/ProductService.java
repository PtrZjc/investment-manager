package pl.zajacp.investmentmanager.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.generics.AbstractCrudService;
import pl.zajacp.investmentmanager.user.UserService;

@Service
@Transactional
public class ProductService extends AbstractCrudService<FinanceProduct> {

    private final UserService userService;

    @Autowired
    public ProductService(ProductRepository repo, UserService userService) {
        this.repo=repo;
        this.userService=userService;
    }

    @Override
    public void save(FinanceProduct product) {
        product.setUser(userService.getLoggedUser());
        repo.save(product);
    }
}
