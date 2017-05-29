package jUnitTests;

import static org.junit.Assert.assertEquals;
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
	public void Constraint_1(){
		Proceedings proc = getValidProceedings();
		proc.setId("conf/hmi/1987");
		List<String> cvs = db.addProceedingValidated(proc);
		for(String cv: cvs){
			System.out.println(cv);
		}
		assert cvs.size()==1;
		
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
	@Test
	public void Constraint_5(){
		Proceedings proc = getValidProceedings();
		proc.setIsbn("0-89791-248-9");
		assertEquals("ISBN set", proc.getNote());
		proc.setIsbn("0-89791-359-9");
		assertEquals("ISBN set\nISBN updated, old value was 0-89791-248-9", proc.getNote());
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
	
	public InProceedings getValidInProceeding(){
//		InProceedings inProc = db.getInProceedingsById("conf/vldb/CeriW90");
		InProceedings inProc = new InProceedings("conf/some/id");
		inProc.setProceedings(new Proceedings("conf/hmi/1987"));
		List<Person> authors = new ArrayList<>();
		authors.add(new Person("Gregory"));
		inProc.setAuthors(authors);
		inProc.setNote("Accepted");
		return inProc;
	}
	
	public Proceedings getValidProceedings(){
		Proceedings proc = new Proceedings("conf/this/does/not/exist");
		proc.setTitle("This is the title");
		proc.setYear(1990);
		List<Person> authors = new ArrayList<>();
		authors.add(new Person("Gregory"));
		proc.setAuthors(authors);
		return proc;
	}
}
