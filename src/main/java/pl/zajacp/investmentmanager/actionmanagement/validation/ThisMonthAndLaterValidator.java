package pl.zajacp.investmentmanager.actionmanagement.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ThisMonthAndLaterValidator
  implements ConstraintValidator<ThisMonthAndLater, LocalDate> {
     
    @Override
    public void initialize(ThisMonthAndLater constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context){
        return LocalDate.now().withDayOfMonth(1).isBefore(date);
    }
}

//