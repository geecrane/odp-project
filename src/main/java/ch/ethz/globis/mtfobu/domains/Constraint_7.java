package ch.ethz.globis.mtfobu.domains;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import annotations.ProceedingExists;
import ch.ethz.globis.mtfobu.odb_project.db.Database;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseMongoDB;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller;


public class Constraint_7 implements ConstraintValidator<ProceedingExists, Proceedings> {

	@Override
	public void initialize(ProceedingExists constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isValid(Proceedings proc, ConstraintValidatorContext context) {
		if (proc==null) return false;
		String id = proc.getId();
		if(Controller.db.getProceedingById(id)==null){
			return false;
		}
		else return true;
	}

}


