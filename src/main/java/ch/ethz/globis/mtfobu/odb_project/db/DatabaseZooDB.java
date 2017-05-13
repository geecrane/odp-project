package ch.ethz.globis.mtfobu.odb_project.db;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Function;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.zoodb.jdo.ZooJdoHelper;
import org.zoodb.tools.ZooHelper;

import ch.ethz.globis.mtfobu.domains.Conference;
import ch.ethz.globis.mtfobu.domains.ConferenceEdition;
import ch.ethz.globis.mtfobu.domains.DomainObject;
import ch.ethz.globis.mtfobu.domains.InProceedings;
import ch.ethz.globis.mtfobu.domains.Person;
import ch.ethz.globis.mtfobu.domains.Proceedings;
import ch.ethz.globis.mtfobu.domains.Publication;
import ch.ethz.globis.mtfobu.domains.Publisher;
import ch.ethz.globis.mtfobu.domains.Series;
import ch.ethz.globis.mtfobu.odb_project.Config;
import javafx.collections.ObservableList;

public class DatabaseZooDB implements Database {
    private final String dbName;
    private PersistenceManager pm;

    private DatabaseZooDB(String dbName) {
	this.dbName = dbName;

	// George: only creates a database automatically if it doesn't already
	// exist.
	// Does not automatically delete existing database!
	if (!ZooHelper.dbExists(dbName)) {
	    create();
	}

	openDB();
    }

    private static class Singleton {
	private static final DatabaseZooDB instance = new DatabaseZooDB(Config.DATABASE_NAME);
    }

    /**
     * Used to retrieve the database instance. DO NOT try to instantiate the
     * Database class manually!
     * 
     * @return The Database instance
     */
    public static DatabaseZooDB getDatabase() {
	return Singleton.instance;
    }

    @Override
    public boolean openDB() {
	this.pm = ZooJdoHelper.openDB(dbName);
	return true;
    }

    @Override
    public void closeDB() {
	if (pm.currentTransaction().isActive()) {
	    pm.currentTransaction().rollback();
	}
	pm.close();
	pm.getPersistenceManagerFactory().close();
    }

    // George: Enables creation/deletion of database on demand.
    public boolean create() {
	// George: Remove the database if it exists
	if (ZooHelper.dbExists(dbName)){
	    if(!ZooHelper.removeDb(dbName)){
		System.err.println(String.format("Could not remove db: %s", dbName));
	    }
	}

	// By default, all database files will be created in %USER_HOME%/zoodb
	ZooHelper.createDb(dbName);
	return true;
    }

    public String getName() {
	return dbName;
    }

    // George: commits the imported XML data to ZooDB
    public void importData(HashMap<String, Proceedings> proceedingsList,
	    HashMap<String, InProceedings> inProceedingsList) {
	pm.currentTransaction().begin();

	// George: commit proceedings
	for (Proceedings proceedings : proceedingsList.values()) {
	    pm.makePersistent(proceedings);
	}

	// George: commit inProceedings too. Because cannot rely on
	// transitivity.
	// Some inProceedings have no proceedings. Probably because the XML
	// provided is not complete.
	for (InProceedings inProceedings : inProceedingsList.values()) {
	    pm.makePersistent(inProceedings);
	}

	// defining some indexes
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
    }

    // George: didn't implement and haven't checked the functions below. Not
    // sure if they work.

    @Override
    @SuppressWarnings("unchecked")
    public Person getPersonById(String id, boolean lazy) {
	Collection<Person> foundPeople = null;
	pm.currentTransaction().begin();
	Query query = pm.newQuery(Person.class, "name == " + id);
	foundPeople = (Collection<Person>) query.execute();
	pm.currentTransaction().commit();

	return foundPeople.iterator().next();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Publisher getPublisherByName(String name) {
	Collection<Publisher> foundPublishers = null;

	pm.currentTransaction().begin();
	Query query = pm.newQuery(Publisher.class, "name == " + name);
	foundPublishers = (Collection<Publisher>) query.execute();
	pm.currentTransaction().commit();

	return foundPublishers.iterator().next();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Conference getConferenceByName(String name, boolean lazy) {
	Collection<Conference> foundConferences = null;

	pm.currentTransaction().begin();
	Query query = pm.newQuery(Conference.class, "name == " + name);
	foundConferences = (Collection<Conference>) query.execute();
	pm.currentTransaction().commit();

	return foundConferences.iterator().next();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Person> getPeople() {
	Collection<Person> people = null;

	pm.currentTransaction().setNontransactionalRead(true);

	Query q = pm.newQuery(Person.class);
	people = (Collection<Person>) q.execute();

	return new ArrayList(people);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<InProceedings> getInProceedings() {
	Collection<InProceedings> proceedings = null;

	pm.currentTransaction().setNontransactionalRead(true);

	Query q = pm.newQuery(InProceedings.class);
	proceedings = (Collection<InProceedings>) q.execute();

	return new ArrayList(proceedings);

    }

    /***
     * Seba: @param fun is the function that will treat the queried data in form
     * of a collection. This function is executed during the open transaction.
     * 
     * @param rangeStart
     *            and @param rangeEnd are optional parameters and determine the
     *            rage of the query
     */
    public void executeOnAllInProceedings(Function<Collection<InProceedings>, Void> fun, OptionalLong rangeStart,
	    OptionalLong rangeEnd) {

	// Seba: default range values in case no range has been specified.
	long begin = rangeStart.isPresent() ? rangeStart.getAsLong() : 0;
	long end = rangeEnd.isPresent() ? rangeEnd.getAsLong() : Long.MAX_VALUE;

	pm.currentTransaction().begin();
	Query q = pm.newQuery(InProceedings.class);
	q.setRange(begin, end);
	fun.apply((Collection<InProceedings>) q.execute());
	pm.currentTransaction().commit();

    }

    /***
     * Seba: @param fun is the function that will treat the queried data in form
     * of a collection. This function is executed during the open transaction.
     * 
     * @param rangeStart
     *            and @param rangeEnd are optional parameters and determine the
     *            rage of the query
     */
    public void executeOnAllPublications(Function<Collection<Publication>, Void> fun, OptionalLong rangeStart,
	    OptionalLong rangeEnd) {

	// Seba: default range values in case no range has been specified.
	long begin = rangeStart.isPresent() ? rangeStart.getAsLong() : 0;
	long end = rangeEnd.isPresent() ? rangeEnd.getAsLong() : Long.MAX_VALUE;

	pm.currentTransaction().begin();
	Query q = pm.newQuery(Proceedings.class);
	q.setRange(begin, end);
	fun.apply((Collection<Publication>) q.execute());
	q.closeAll();
	q = pm.newQuery(InProceedings.class);
	q.setRange(begin, end);
	fun.apply((Collection<Publication>) q.execute());

	pm.currentTransaction().commit();

    }

    public long executeOnObjectById(long objID, Function<Object, Integer> fun) {

	long val = 0;
	pm.currentTransaction().begin();
	val = fun.apply(pm.getObjectById(objID));

	return val;
    }

    // obsolete
    // public void removePersonByID(long objID){
    // PersistenceManager pm = ZooJdoHelper.openDB(dbName);
    // pm.currentTransaction().begin();
    // Person person = (Person) pm.getObjectById(objID);
    // person.removeReferencesFromOthers();
    // pm.deletePersistent(person);
    // closeDB(pm);
    // }
    /**
     * Seba: Removes every object given the object ID by @param objID. It
     * assumes that the object implements the DomainObject interface. Otherwise
     * the object will not be removed and the incident reported in the error log
     */
    public void removeObjectById(long objID) {

	pm.currentTransaction().begin();
	Object obj = pm.getObjectById(objID);

	// Seba: The function "removeReferencesFromOthers" is part of the
	// "DomainObject" interface that every persistent class should implement
	if (obj instanceof DomainObject) {
	    ((DomainObject) obj).removeReferencesFromOthers();
	    pm.deletePersistent(obj);
	} else if (obj != null) {
	    System.err.printf(
		    "Database was not expected to remove an object that does not implement DomainObject.\nThe give object ID was: %l \nits class was: %s",
		    objID, obj.getClass());
	}

	pm.currentTransaction().commit();

    }

    public void executeOnPublicationsByTitle(String title, Function<ArrayList<Publication>, Void> fun) {

	ArrayList<Publication> pubs = new ArrayList();

	pm.currentTransaction().begin();
	Query q = pm.newQuery(Proceedings.class, "title.indexOf('" + title + "') > -1");
	pubs.addAll((Collection<Publication>) q.execute());
	q.closeAll();
	q = pm.newQuery(InProceedings.class, "title.indexOf('" + title + "') > -1");
	pubs.addAll((Collection<Publication>) q.execute());
	fun.apply(pubs);
	pm.currentTransaction().commit();

    }

    /**
     * Close the database connection.
     * 
     * @param pm
     *            The current PersistenceManager.
     */

    @Override
    public List<Publisher> getPublishers() {
	Collection<Publisher> pubs = null;

	pm.currentTransaction().setNontransactionalRead(true);

	Query q = pm.newQuery(Publisher.class);
	pubs = (Collection<Publisher>) q.execute();

	return new ArrayList(pubs);
    }

    @Override
    public List<Publication> getPublications() {
	Collection<Publication> publs = null;

	pm.currentTransaction().setNontransactionalRead(true);

	Query q = pm.newQuery(Publication.class);
	publs = (Collection<Publication>) q.execute();

	return new ArrayList(publs);
    }

    @Override
    public List<Proceedings> getProceedings() {
	Collection<Proceedings> proceedings = null;

	pm.currentTransaction().setNontransactionalRead(true);

	Query q = pm.newQuery(Proceedings.class);
	proceedings = (Collection<Proceedings>) q.execute();

	return new ArrayList(proceedings);
    }

    @Override
    public List<Conference> getConferences() {
	Collection<Conference> confs = null;

	pm.currentTransaction().setNontransactionalRead(true);

	Query q = pm.newQuery(Conference.class);
	confs = (Collection<Conference>) q.execute();

	return new ArrayList(confs);
    }

    @Override
    public List<Series> getSeries() {
	Collection<Series> s = null;

	pm.currentTransaction().setNontransactionalRead(true);

	Query q = pm.newQuery(Series.class);
	s = (Collection<Series>) q.execute();

	return new ArrayList(s);
    }

    @Override
    public List<ConferenceEdition> getConferenceEditions() {
	Collection<ConferenceEdition> ce = null;

	pm.currentTransaction().setNontransactionalRead(true);

	Query q = pm.newQuery(ConferenceEdition.class);
	ce = (Collection<ConferenceEdition>) q.execute();

	return new ArrayList(ce);
    }

    @Override
    public InProceedings getInProceedingsById(String id) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Proceedings getProceedingById(String id) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Publication getPublicationById(String id) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Set<Publication> getAuthoredPublications(String personName, boolean lazy) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Set<Publication> getEditedPublications(String personName, boolean lazy) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Set<ConferenceEdition> getConfEditionsForConf(Conference conference) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Series getSeriesByName(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void addProceeding(Proceedings proc) {
	// TODO Auto-generated method stub

    }

    @Override
    public void deleteProceedingById(String id) {
	// TODO Auto-generated method stub

    }

    @Override
    public void updateProceeding(Proceedings proc) {
	// TODO Auto-generated method stub

    }

    @Override
    public void addInProceeding(InProceedings inProc) {
	// TODO Auto-generated method stub

    }

    @Override
    public void deleteInProceedingById(String id) {
	// TODO Auto-generated method stub

    }

    @Override
    public List<Publication> getPublicationsByFilter(String title, int begin_offset, int end_offset) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<Person> getCoAuthors(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int authorDistance(String authorIdA, String authorIdB) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public double getAvgAuthorsInProceedings() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public List<String> getNumberPublicationsPerYearInterval(int yearLowerBound, int yearUpperBound) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int getNumberOfPublicationsPerConferenceByName(String conferenceName) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public int countEditorsAndAuthorsOfConferenceByName(String conferenceName) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public List<Person> getAllAuthorsOfConferenceByName(String conferenceName) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<Publication> getAllPublicationsOfConferenceByName(String conferenceName) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<Person> getPeopleThatAreAuthorsAndEditors() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<InProceedings> getPublicationsWhereAuthorIsLast(String authorName) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<Publisher> task14(int yearLowerBound, int yearUpperBound) {
	// TODO Auto-generated method stub
	return null;
    }

}
