package pl.zajacp.investmentmanager.products.validation;

import pl.zajacp.investmentmanager.products.savings.SavingsAccount;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class InterestAboveLimitPresentValidator
        implements ConstraintValidator<InterestAboveLimitPresent, SavingsAccount> {

    @Override
    public void initialize(InterestAboveLimitPresent constraintAnnotation) {
    }

    @Override
    public boolean isValid(SavingsAccount savingsAccount, ConstraintValidatorContext constraintValidatorContext) {

        if (savingsAccount.getValueLimit() == null) {
            return true;
        }
        return savingsAccount.getInterestAboveLimit() != null;

    }
}