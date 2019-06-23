package pl.zajacp.investmentmanager.products.validation;

import pl.zajacp.investmentmanager.products.SavingsAccount;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LessInterestAboveLimitValidator
        implements ConstraintValidator<LessInterestAboveLimit, SavingsAccount> {

    @Override
    public void initialize(LessInterestAboveLimit constraintAnnotation) {
    }

    @Override
    public boolean isValid(SavingsAccount savingsAccount, ConstraintValidatorContext constraintValidatorContext) {

        return savingsAccount.getInterest().compareTo(savingsAccount.getInterestAboveLimit())>0;
    }
}