package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.zoodb.jdo.ZooJdoHelper;
import org.zoodb.tools.ZooHelper;

public class Database {
	public Database(String dbName) throws DatabaseNameCollisionException{
		// remove database if it exists
		if (ZooHelper.dbExists(dbName)) {
			throw new DatabaseNameCollisionException("There already exists a database called: " + dbName);

		}
		else{
			this.dbName = dbName;
			// create database
			// By default, all database files will be created in %USER_HOME%/zoodb
			ZooHelper.createDb(dbName);
		}



		ZooHelper.removeDb(dbName);
	}
	public String getName(){
		return dbName;
	}
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

class DatabaseNameCollisionException extends Exception{
	private static final long serialVersionUID = -86080104427990869L;

	//Parameterless Constructor
	public DatabaseNameCollisionException() {}

	//Constructor that accepts a message
	public DatabaseNameCollisionException(String message)
	{
		super(message);
	}
}
class DatabaseUnexpectedEntry extends Exception{
	//Parameterless Constructor
	public DatabaseUnexpectedEntry() {}

	//Constructor that accepts a message
	public DatabaseUnexpectedEntry(String message)
	{
		super(message);
	}
}
