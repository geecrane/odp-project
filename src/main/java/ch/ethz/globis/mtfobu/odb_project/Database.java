package ch.ethz.globis.mtfobu.odb_project;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.OptionalLong;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.zoodb.jdo.ZooJdoHelper;
import org.zoodb.tools.ZooHelper;

public class Database {
	public final int PAGE_SIZE = 20;
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
	
	public static boolean testForExistance(String dbName){
		return ZooHelper.dbExists(dbName);
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
		ZooJdoHelper.createIndex(pm, Proceedings.class, "id", true);
		ZooJdoHelper.createIndex(pm, Proceedings.class, "title", false);
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
	
	public void queryInProceedings(Consumer<Collection<InProceedings>> fun, QueryParameters p){
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		pm.currentTransaction().begin();
		
		Query q;
		
		if (p.isSearch) {
			q = pm.newQuery(InProceedings.class, "title.indexOf('" + p.searchTerm + "') > -1");
		} else {
			q = pm.newQuery(InProceedings.class);
		}
		
		if (p.isRanged) {
			long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
			long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() : Long.MAX_VALUE;
			q.setRange(begin, end);
		} else {
			q.setRange((p.pageNumber-1) * PAGE_SIZE, p.pageNumber * PAGE_SIZE);
		}
		
		fun.accept((Collection<InProceedings>) q.execute());
		pm.currentTransaction().commit();

		closeDB(pm);
	}
	
	
	// Still working on this JOEL
	public class QueryHelper<T extends DomainObject> {
		Class<T> classImmediate;
		String searchableField;
		
		public QueryHelper(Class<T> classImmediate, String searchableField) {
			this.classImmediate = classImmediate;
			this.searchableField = searchableField;
		}
		
		public void queryForDomainObject(Consumer<Collection<T>> fun, QueryParameters p){
			PersistenceManager pm = ZooJdoHelper.openDB(dbName);
			pm.currentTransaction().begin();
			
			Query q;
			
			if (p.isSearch) {
				q = pm.newQuery(classImmediate, searchableField + ".indexOf('" + p.searchTerm + "') > -1");
			} else {
				q = pm.newQuery(classImmediate);
			}
			
			if (p.isRanged) {
				long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
				long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() : Long.MAX_VALUE;
				q.setRange(begin, end);
			} else {
				q.setRange((p.pageNumber-1) * PAGE_SIZE, p.pageNumber * PAGE_SIZE);
			}
			
			fun.accept((Collection<T>) q.execute());
			pm.currentTransaction().commit();

			closeDB(pm);
		}
	}
	
	/*** Seba: @param fun is the function that will treat the queried data in form of a collection. 
	 * This function is executed during the open transaction.
	 * @param rangeStart and @param rangeEnd are optional parameters and determine the rage of the query */
	public void executeOnAllInProceedings(Consumer<Collection<InProceedings>> fun, OptionalLong rangeStart, OptionalLong rangeEnd){
		
		//Seba: default range values in case no range has been specified.
		long begin = rangeStart.isPresent() ? rangeStart.getAsLong() : 0;
		long end = rangeEnd.isPresent() ? rangeEnd.getAsLong() : Long.MAX_VALUE;
		
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);

		pm.currentTransaction().begin();
		Query q = pm.newQuery(InProceedings.class);
		q.setRange(begin, end);
		fun.accept((Collection<InProceedings>) q.execute());
		pm.currentTransaction().commit();

		closeDB(pm);
	}
	
	/*** Seba: @param fun is the function that will treat the queried data in form of a collection. 
	 * This function is executed during the open transaction.
	 * @param rangeStart and @param rangeEnd are optional parameters and determine the rage of the query */
	public void executeOnAllPublications(Function<Collection<Publication>,Void> fun, OptionalLong rangeStart, OptionalLong rangeEnd){
		
		//Seba: default range values in case no range has been specified.
		long begin = rangeStart.isPresent() ? rangeStart.getAsLong() : 0;
		long end = rangeEnd.isPresent() ? rangeEnd.getAsLong() : Long.MAX_VALUE;
		
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);

		pm.currentTransaction().begin();
		Query q = pm.newQuery(Proceedings.class);
		q.setRange(begin, end);
		fun.apply((Collection<Publication>) q.execute());
		q.closeAll();
		q = pm.newQuery(InProceedings.class);
		q.setRange(begin, end);
		fun.apply((Collection<Publication>) q.execute());
		
		pm.currentTransaction().commit();

		closeDB(pm);
	}
	
	
	public long executeOnObjectById(long objID, Function<Object, Integer> fun){
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		long val = 0;
		pm.currentTransaction().begin();
		val = fun.apply(pm.getObjectById(objID));
		closeDB(pm);
		return val;
	}
	
	//obsolete
//	public void removePersonByID(long objID){
//		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
//		pm.currentTransaction().begin();
//		Person person = (Person) pm.getObjectById(objID);
//		person.removeReferencesFromOthers();
//		pm.deletePersistent(person);
//		closeDB(pm);
//	}
	/** Seba: Removes every object given the object ID by @param objID. 
	 * It assumes that the object implements the DomainObject interface. 
	 * Otherwise the object will not be removed and the incident reported in the error log */
	public void removeObjectById(long objID){
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);
		
		pm.currentTransaction().begin();
		Object obj = pm.getObjectById(objID);
		
		//Seba: The function "removeReferencesFromOthers" is part of the "DomainObject" interface that every persistent class should implement
		if (obj instanceof DomainObject){
			((DomainObject) obj).removeReferencesFromOthers();
			pm.deletePersistent(obj);
		}
		else if (obj != null) {
			System.err.printf("Database was not expected to remove an object that does not implement DomainObject.\nThe give object ID was: %l \nits class was: %s", objID, obj.getClass());
		}
		
		pm.currentTransaction().commit();
		closeDB(pm);
	}
	
public void executeOnPublicationsByTitle(String title, Function<ArrayList<Publication>,Void> fun){
			
		ArrayList<Publication> pubs = new ArrayList();
		PersistenceManager pm = ZooJdoHelper.openDB(dbName);

		pm.currentTransaction().begin();
		Query q = pm.newQuery(Proceedings.class, "title.indexOf('" + title + "') > -1");
		pubs.addAll((Collection<Publication>) q.execute());
		q.closeAll();
		q = pm.newQuery(InProceedings.class, "title.indexOf('" + title + "') > -1");
		pubs.addAll((Collection<Publication>) q.execute());
		fun.apply(pubs);
		pm.currentTransaction().commit();
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
	//private PersistenceManager pm;

}
