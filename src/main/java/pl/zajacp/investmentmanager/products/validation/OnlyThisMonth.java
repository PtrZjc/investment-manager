package pl.zajacp.investmentmanager.products.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = OnlyThisMonthValidator.class)
@Documented
public @interface OnlyThisMonth {
    String message() default "{OnlyThisMonth.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
