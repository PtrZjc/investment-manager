package pl.zajacp.investmentmanager.actionmanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.zajacp.investmentmanager.products.FinanceProduct;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {

    List<Action> findByProductOrderByActionDateAscAfterActionValueAsc(FinanceProduct product);

}