package ch.ethz.globis.mtfobu.domains;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import annotations.AuthorsValid;

public class Constraint_4 implements ConstraintValidator<AuthorsValid, List<Person>> {

	@Override
	public void initialize(AuthorsValid constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isValid(List<Person> authors, ConstraintValidatorContext context) {
		// TODO Auto-generated method stub
		return false;
	}

}
