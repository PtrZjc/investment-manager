package pl.zajacp.investmentmanager.products.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.YearMonth;

public class OnlyThisMonthValidator
  implements ConstraintValidator<OnlyThisMonth, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context){
        if(date==null){
            return false;
        }
        return YearMonth.now().minusMonths(1).atEndOfMonth().isBefore(date) &&
                YearMonth.now().plusMonths(1).atDay(1).isAfter(date);
    }
}
