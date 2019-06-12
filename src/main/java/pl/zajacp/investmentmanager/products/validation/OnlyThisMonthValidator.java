package pl.zajacp.investmentmanager.products.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

//TODO implement future month condition
public class OnlyThisMonthValidator
  implements ConstraintValidator<OnlyThisMonth, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context){
        return LocalDate.now().withDayOfMonth(1).isBefore(date);
    }
}
