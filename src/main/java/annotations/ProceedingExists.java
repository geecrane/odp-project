package annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import ch.ethz.globis.mtfobu.domains.Constraint_7;

//Linking the validator I had shown above.
@Constraint(validatedBy = { Constraint_7.class })
// This constraint annotation can be used only on fields and method parameters.
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface ProceedingExists {

	// The message to return when the instance of MyAddress fails the
	// validation.
	String message() default "Proceeding does not exist";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}