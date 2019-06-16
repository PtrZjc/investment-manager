package pl.zajacp.investmentmanager.actionmanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.zajacp.investmentmanager.products.FinanceProduct;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {

    List<Action> findByProductOrderByActionDateAscAfterActionValueAsc(FinanceProduct product);

    @Query("select a from Action a where a.isDone = false order by a.actionDate desc, a.afterActionValue asc")
    List<Action> findChronologicalActive(FinanceProduct product);
}