package pl.zajacp.investmentmanager.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.zajacp.investmentmanager.user.User;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<FinanceProduct, Long> {
    List<FinanceProduct> findAllByUserOrderByCreatedDesc(User user);
}
