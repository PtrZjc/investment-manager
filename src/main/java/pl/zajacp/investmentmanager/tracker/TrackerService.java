package pl.zajacp.investmentmanager.tracker;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.products.ProductRepository;
import pl.zajacp.investmentmanager.products.SavingsAccount;

@Service
public class TrackerService {

    private final ActionService actionService;
    private final ProductRepository productRepository;

    public TrackerService(ActionService actionService, ProductRepository productRepository) {
        this.actionService = actionService;
        this.productRepository = productRepository;
    }

    @Scheduled(cron = "0 0 3 1 * ? *")
    public void summarizeLastMonth() {
        productRepository.findAll().stream()
                .filter(p -> p.getClass() == SavingsAccount.class)
                .map(p->(SavingsAccount) p)
                .forEach(actionService::closeOldActions);
    }
}
