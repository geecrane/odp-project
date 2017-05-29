package ch.ethz.globis.mtfobu.domains;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import annotations.ProceedingExists;
import ch.ethz.globis.mtfobu.odb_project.db.Database;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseMongoDB;


public class Constraint_7 implements ConstraintValidator<ProceedingExists, Proceedings> {

	@Override
	public void initialize(ProceedingExists constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isValid(Proceedings proc, ConstraintValidatorContext context) {
		if (proc==null) return false;
		String id = proc.getId();
		Database db = new DatabaseMongoDB();
		if(db.getProceedingById(id)==null){
			return false;
		}
		else return true;
	}

}


