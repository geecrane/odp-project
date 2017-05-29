package ch.ethz.globis.mtfobu.domains;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import annotations.IDValid;
import ch.ethz.globis.mtfobu.odb_project.db.Database;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseMongoDB;
import ch.ethz.globis.mtfobu.domains.Publication;

public class Constraint_1 implements ConstraintValidator<IDValid, String> {

	@Override
	public void initialize(IDValid constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}
	//DON't use this
	@Override
	public boolean isValid(String id, ConstraintValidatorContext context) {
		Database db = new DatabaseMongoDB();
		Publication pub = db.getPublicationById(id);
		return false;
	}

}
