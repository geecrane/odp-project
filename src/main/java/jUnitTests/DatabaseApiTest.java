package jUnitTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.Test;

import ch.ethz.globis.mtfobu.domains.Proceedings;
import ch.ethz.globis.mtfobu.domains.Publisher;
import ch.ethz.globis.mtfobu.odb_project.db.Database;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseMongoDB;

public class DatabaseApiTest {
	private Database db;

	public DatabaseApiTest() {
		System.out.println("Testing: MongoDB");
		DatabaseMongoDB.verbose = true;
		db = new DatabaseMongoDB();

		// test if db exists
		assertNotEquals(null, db);

		db.openDB();
	}

	@Test
	public void testGetPublishersAndGetPublisherByName() {
		// test getPublishers
		List<Publisher> pubs = db.getPublishers();
		assertEquals(93, pubs.size());
		for (Publisher pub : pubs) {
			// test getPublisherByName
			Publisher foundPub = db.getPublisherByName(pub.getName());
			assertNotEquals(null, foundPub);
			assertEquals(pub.getId(), foundPub.getId());
		}

	}

	@Test
	public void testGetProceedings() {
		// test getProceedings
		List<Proceedings> procs = db.getProceedings();
		assertEquals(1259, procs.size());
		for (Proceedings proc : procs) {
			assertNotEquals(null, proc.getId());
			assertNotEquals(null, proc.getTitle());
			//assertNotEquals(null, proc.getPublisher()); This assertion id wrong example: conf/ijcai/1975
			assertNotEquals(null, proc.getPublications());
			assertNotEquals(null, proc.getYear());
		}
	}
}
