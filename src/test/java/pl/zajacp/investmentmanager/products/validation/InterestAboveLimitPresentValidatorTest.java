package pl.zajacp.investmentmanager.products.validation;

import org.hibernate.validator.HibernateValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;

import javax.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class InterestAboveLimitPresentValidatorTest {

    private LocalValidatorFactoryBean localValidatorFactory;

    @Before
    public void setup() {
        localValidatorFactory = new LocalValidatorFactoryBean();
        localValidatorFactory.setProviderClass(HibernateValidator.class);
        localValidatorFactory.afterPropertiesSet();
    }

    @Test
    public void shouldViolateOnInterestAboveLimit() {
        final SavingsAccount account = new SavingsAccount();
        account.setBank("Bank");
        account.setValidityDate(LocalDate.now().plusMonths(1));
        account.setValue(new BigDecimal(1000));
        account.setInterest(new BigDecimal(0.05));
        account.setValueLimit(new BigDecimal(500));
        //lack of InterestAboveLimit necesarry when valueLimit is present
        Set<ConstraintViolation<SavingsAccount>> constraintViolations = localValidatorFactory.validate(account);
        Assert.assertTrue("Expected validation error not found", constraintViolations.size() == 1);
    }

}