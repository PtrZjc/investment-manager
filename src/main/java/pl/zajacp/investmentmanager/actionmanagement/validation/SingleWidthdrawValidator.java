package pl.zajacp.investmentmanager.actionmanagement.validation;

import pl.zajacp.investmentmanager.actionmanagement.ActionDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SingleWidthdrawValidator
  implements ConstraintValidator<SingleWidthdraw, ActionDto> {
     
    @Override
    public void initialize(SingleWidthdraw constraintAnnotation) {
    }

    @Override
    public boolean isValid(ActionDto actionDto, ConstraintValidatorContext context){
        if(actionDto.getIsNegative() == null || actionDto.getIsSingle() == null){
            return false;
        }
        return !(actionDto.getIsNegative() && !actionDto.getIsSingle());
    }
}