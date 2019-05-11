package pl.zajacp.investmentmanager.products;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<FinanceProduct, Long> {

}
