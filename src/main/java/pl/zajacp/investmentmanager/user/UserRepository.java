package pl.zajacp.investmentmanager.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.zajacp.investmentmanager.investments.FinanceProduct;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
