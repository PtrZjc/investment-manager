package pl.zajacp.investmentmanager.actionmanagement.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = SingleWidthdrawValidator.class)
@Documented
public @interface SingleWidthdraw {
    String message() default "{SingleWidthdraw.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
