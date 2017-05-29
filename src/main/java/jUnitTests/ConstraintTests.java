package jUnitTests;

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.ethz.globis.mtfobu.domains.InProceedings;
import ch.ethz.globis.mtfobu.domains.Person;
import ch.ethz.globis.mtfobu.domains.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.db.Database;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseMongoDB;


public class ConstraintTests {
	private Database db;

	public ConstraintTests() {
		System.out.println("Testing: Constraints");
		DatabaseMongoDB.verbose = true;
		db = new DatabaseMongoDB();

		// test if db exists
		assertNotEquals(null, db);

		db.openDB();
	}
	
	@Test
	public void Constraint_7(){
		InProceedings inProc = getValidInProceeding();
		List<String> cvs = db.validateInProceedings(inProc);
		for(String cv: cvs){
			System.out.println(cv);
		}
		assert cvs.isEmpty();
	}
	
	@Test
	public void Constraint_4(){
		
		//InProceedings
		InProceedings inProc = getValidInProceeding();
		List<String> cvs = db.validateInProceedings(inProc);
		assert cvs.isEmpty();
		
		inProc.setAuthors(new ArrayList<>());
		cvs = db.validateInProceedings(inProc);
		assert cvs.size()==1;
		
		inProc.setAuthors(null);
		cvs = db.validateInProceedings(inProc);
		assert cvs.size()==1;
	}
	
	InProceedings getValidInProceeding(){
//		InProceedings inProc = db.getInProceedingsById("conf/vldb/CeriW90");
		InProceedings inProc = new InProceedings("/conf/some/id");
		inProc.setProceedings(new Proceedings("conf/hmi/1987"));
		List<Person> authors = new ArrayList<>();
		authors.add(new Person("Gregory"));
		inProc.setAuthors(authors);
		inProc.setNote("Accepted");
		return inProc;
	}
}
