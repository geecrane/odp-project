package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.zoodb.jdo.ZooJdoHelper;
import org.zoodb.tools.ZooHelper;

public class Database {
	
	public Database(String dbName){
		this.dbName = dbName;
		
		//George: only creates a database automatically if it doesn't already exist.
		//Does not automatically delete existing database!
		if (!ZooHelper.dbExists(dbName)) {
			create();
		}
	}
	
	//George: Enables creation/deletion of database on demand.
	public void create() {
		//George: Remove the database if it exists
		if (ZooHelper.dbExists(dbName))
			ZooHelper.removeDb(dbName);

		// By default, all database files will be created in %USER_HOME%/zoodb
		ZooHelper.createDb(dbName);
	}
	
	public String getName(){
		return dbName;
	}
	
	
	//George: commits the imported XML data to ZooDB
	public void importData(HashMap<String, Proceedings> proceedingsList, HashMap<String, InProceedings> inProceedingsList){
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		pm.currentTransaction().begin();
		
		//George: commit proceedings
	    for (Proceedings proceedings : proceedingsList.values()) {
	    	pm.makePersistent(proceedings);
	    }
	    
	    //George: commit inProceedings too. Because cannot rely on transitivity.
	    //Some inProceedings have no proceedings. Probably because the XML provided is not complete.
	    for (InProceedings inProceedings : inProceedingsList.values()) {
	    	pm.makePersistent(inProceedings);
	    }
		
		//defining some indexes
		ZooJdoHelper.createIndex(pm, Person.class, "name", false);
		ZooJdoHelper.createIndex(pm, InProceedings.class, "id", true);
		ZooJdoHelper.createIndex(pm, InProceedings.class, "title", false);
		ZooJdoHelper.createIndex(pm, InProceedings.class, "id", true);
		ZooJdoHelper.createIndex(pm, InProceedings.class, "title", false);
		ZooJdoHelper.createIndex(pm, Publisher.class, "id", true);
		ZooJdoHelper.createIndex(pm, Series.class, "name", false);
		ZooJdoHelper.createIndex(pm, Conference.class, "id", true);
		ZooJdoHelper.createIndex(pm, ConferenceEdition.class, "id", true);
		
		pm.currentTransaction().commit();
		closeDB(pm);
	}
	
	//George: didn't implement and haven't checked the functions below. Not sure if they work.
	@SuppressWarnings("unchecked")
	public Collection<Person> getPeopleByName (String name){
		Collection<Person> foundPeople = null;
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		
		pm.currentTransaction().begin();
		Query query = pm.newQuery(Person.class, "name == " + name);
		foundPeople = (Collection<Person>) query.execute();	
		pm.currentTransaction().commit();
		closeDB(pm);

		return foundPeople;	
	}
	@SuppressWarnings("unchecked")
	public Collection<Publisher> getPublisherByName (String name)
	{
		Collection<Publisher> foundPublishers = null;
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		
		pm.currentTransaction().begin();
		Query query = pm.newQuery(Publisher.class, "name == " + name);
		foundPublishers = (Collection<Publisher>) query.execute();	
		pm.currentTransaction().commit();
		closeDB(pm);

		return foundPublishers;	
	}
	@SuppressWarnings("unchecked")
	public Collection<Conference> getConferencesByName (String name)
	{
		Collection<Conference> foundConferences = null;
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		
		pm.currentTransaction().begin();
		Query query = pm.newQuery(Conference.class, "name == " + name);
		foundConferences = (Collection<Conference>) query.execute();	
		pm.currentTransaction().commit();
		closeDB(pm);

		return foundConferences;	
	}
	
	public Collection<Person> getAllPeople(){
		Collection<Person> people = null;
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		
		pm.currentTransaction().setNontransactionalRead(true);
		
		Query q = pm.newQuery(Person.class);
		people = (Collection<Person>)q.execute(); 

        closeDB(pm);
        
        return Collections.unmodifiableCollection(people);
	}
	
	public Collection<InProceedings> getAllProceedings(){
		Collection<InProceedings> proceedings = null;
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		
		pm.currentTransaction().setNontransactionalRead(true);
		
		Query q = pm.newQuery(InProceedings.class);
		proceedings = (Collection<InProceedings>)q.execute(); 

		closeDB(pm);
        
        return Collections.unmodifiableCollection(proceedings);
		
	}
	
	public Collection<InProceedings> getAllInProceedings(){
		Collection<InProceedings> inproceedings = null;
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		
		pm.currentTransaction().setNontransactionalRead(true);
		
		Query q = pm.newQuery(InProceedings.class);
		inproceedings = (Collection<InProceedings>)q.execute(); 

		closeDB(pm);
        
        return Collections.unmodifiableCollection(inproceedings);
		
	}
	public void removePersonByID(long objID){
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		pm.currentTransaction().begin();
		Person person = (Person) pm.getObjectById(objID);
		person.removeReferencesFromOthers();
		pm.deletePersistent(person);
		closeDB(pm);
	}
	
	/**
	 * Close the database connection.
	 * 
	 * @param pm The current PersistenceManager.
	 */
	private static void closeDB(PersistenceManager pm) {
		if (pm.currentTransaction().isActive()) {
			pm.currentTransaction().rollback();
		}
		pm.close();
		pm.getPersistenceManagerFactory().close();
	}	
	private final String dbName;


}
