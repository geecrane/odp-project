package ch.ethz.globis.mtfobu.odb_project;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import ch.ethz.globis.mtfobu.odb_project.QueryParameters;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;





public class Database {
	private final String dbName;
	private final MongoClient mongoClient;
	private MongoDatabase mongoDB;
	
	public Database(String dbName){
		this.dbName = dbName;
		
		//George: If dbName doesn't exist it will be created automatically
		//once documents are inserted
		mongoClient = new MongoClient();
		mongoDB = mongoClient.getDatabase(dbName);
		
	}
	
	//George: This will drop the database if already exists, 
	//and create a new one automatically when documents are inserted
	public void create() {
		mongoDB.drop();
	}
	
	//George: get inProceedings by id
	public InProceedings getInProceedingsById(String id){
		//TODO:implement
		return null;
	}
	
	//George: get person by id
	public Person getPersonById(String id){
		//TODO:implement
		return null;
	}
	
	//George: get publisher by id
	public Publisher getPublisherById(String series_id){
		//TODO:implement
		return null;
	}
	
	//George: get publications for a series by series id
	public Set<Publication> getSeriesPublicationsById(String series_id){
		//TODO:implement
		return null;
	}
	
	//George: get ConferenceEdition By ID
	public ConferenceEdition getConferenceEditionById(String id){
		//TODO:implement
		return null;
	}
	
	//George: get Conference By ID
	public Conference getConferenceById(String id){
		//TODO:implement
		return null;
	}
	
	//George: Get Series by ID
	public Series getSeriesById(String id){
		MongoCollection<Document> collection = mongoDB.getCollection(Config.SERIES_COLLECTION);
		Document doc = collection.find(eq("_id", id)).first();
		
		if(doc != null){
			String name = doc.get(Config.SERIES_NAME).toString();
			Set<Publication> publications = getSeriesPublicationsById(id);
			Series s = new Series(name);
			s.setPublications(publications);
			return s;
			
		}else{
			return null;
		}
		
	}
	
	// Helper class to construct a function for querying or searching with paging or ranged accesses for any domain object
	public class QueryHelper<DO extends DomainObject> {
		private String collectionToUse;
		private String fieldToSearch;
		private Function<Document,DO> parser;
		
		public QueryHelper(String collectionToUse, String fieldToSearch, Function<Document,DO> parser) {
			this.collectionToUse = collectionToUse;
			this.fieldToSearch = fieldToSearch;
			this.parser = parser;
		}
		
		public Collection<DO> queryForDomainObject(QueryParameters p) {
			MongoCollection<Document> collection = mongoDB.getCollection(collectionToUse);
			MongoCursor<Document> cursor;
			
			if (p.isRanged) {
				long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
				long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() : Long.MAX_VALUE;
				
				if (p.isSearch) {
					cursor = collection.find(text(p.searchTerm)).sort(ascending(fieldToSearch)).skip((int) begin).limit((int) (end - begin)).iterator();
				} else {
					cursor = collection.find().sort(ascending(fieldToSearch)).skip((int) begin).limit((int) (end - begin)).iterator();
				}
				
			} else {
				
				if (p.isSearch) {
					cursor = collection.find(text(p.searchTerm)).sort(ascending(fieldToSearch)).skip((p.pageNumber - 1)* Config.PAGE_SIZE).limit(Config.PAGE_SIZE).iterator();
				} else {
					cursor = collection.find().sort(ascending(fieldToSearch)).skip((p.pageNumber - 1)* Config.PAGE_SIZE).iterator();
				}
				
			}
			
			Collection<DO> coll = new ArrayList<DO>(Config.PAGE_SIZE);
			
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				coll.add(parser.apply(doc));
			}
	
			return coll;
		}
	}
	
	
	// Special case for the Publications Query
	public Collection<Publication> queryForPublications(QueryParameters p) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		MongoCursor<Document> cursor;
		
		if (p.isRanged) {
			long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
			long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() : Long.MAX_VALUE;
			
			if (p.isSearch) {
				cursor = collection.find(text(p.searchTerm)).sort(ascending(Config.INPROCEEDINGS_TITLE)).skip((int) begin).limit((int) (end - begin)).iterator();
			} else {
				cursor = collection.find().sort(ascending(Config.INPROCEEDINGS_TITLE)).skip((int) begin).limit((int) (end - begin)).iterator();
			}
			
		} else {
			
			if (p.isSearch) {
				cursor = collection.find(text(p.searchTerm)).sort(ascending(Config.INPROCEEDINGS_TITLE)).skip((p.pageNumber - 1)* Config.PAGE_SIZE).limit(Config.PAGE_SIZE).iterator();
			} else {
				cursor = collection.find().sort(ascending(Config.INPROCEEDINGS_TITLE)).skip((p.pageNumber - 1)* Config.PAGE_SIZE).iterator();
			}
			
		}
		
		List<Publication> list = new ArrayList<Publication>(2*Config.PAGE_SIZE);
		
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			list.add(makeInProceedingsObject(doc));
		}
		
		collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		
		if (p.isRanged) {
			long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
			long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() : Long.MAX_VALUE;
			
			if (p.isSearch) {
				cursor = collection.find(text(p.searchTerm)).sort(ascending(Config.PROCEEDINGS_TITLE)).skip((int) begin).limit((int) (end - begin)).iterator();
			} else {
				cursor = collection.find().sort(ascending(Config.PROCEEDINGS_TITLE)).skip((int) begin).limit((int) (end - begin)).iterator();
			}
			
		} else {
			
			if (p.isSearch) {
				cursor = collection.find(text(p.searchTerm)).sort(ascending(Config.PROCEEDINGS_TITLE)).skip((p.pageNumber - 1)* Config.PAGE_SIZE).limit(Config.PAGE_SIZE).iterator();
			} else {
				cursor = collection.find().sort(ascending(Config.PROCEEDINGS_TITLE)).skip((p.pageNumber - 1)* Config.PAGE_SIZE).iterator();
			}
			
		}
		
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			list.add(makeProceedingsObject(doc));
		}

		// sorting the combined result list again, otherwise all InProceedings are at the top
		list.sort(java.util.Comparator.comparing(publication -> publication.getTitle()));
		
		return list;
	}
	
	// Query Helper for Proceedings
	public QueryHelper<ConferenceEdition> conferenceEditionQueryHelper = new QueryHelper<ConferenceEdition>(Config.CONFERENCE_EDITION_COLLECTION, Config.CONFERENCE_EDITION_YEAR, this::makeConferenceEditionObject);
	public QueryHelper<Conference> conferenceQueryHelper = new QueryHelper<Conference>(Config.CONFERENCE_COLLECTION, Config.CONFERENCE_NAME, this::makeConferenceObject);
	public QueryHelper<InProceedings> inProceedingsQueryHelper = new QueryHelper<InProceedings>(Config.INPROCEEDINGS_COLLECTION, Config.INPROCEEDINGS_TITLE, this::makeInProceedingsObject);
	public QueryHelper<Person> personQueryHelper = new QueryHelper<Person>(Config.PEOPLE_COLLECTION, Config.PEOPLE_NAME, this::makePersonObject);
	public QueryHelper<Proceedings> proceedingsQueryHelper = new QueryHelper<Proceedings>(Config.PROCEEDINGS_COLLECTION, Config.PROCEEDINGS_TITLE, this::makeProceedingsObject);
	   // No publication query handler, it's a bit of a special case
	public QueryHelper<Publisher> publisherQueryHelper = new QueryHelper<Publisher>(Config.PUBLISHER_COLLECTION, Config.PUBLISHER_NAME, this::makePublisherObject);
	public QueryHelper<Series> seriesQueryHelper = new QueryHelper<Series>(Config.SERIES_COLLECTION, Config.SERIES_NAME, this::makeSeriesObject);
	
	
	
	private ConferenceEdition makeConferenceEditionObject(Document doc) {
		//TODO:implement
		return null;
	}
	
	private Conference makeConferenceObject(Document doc) {
		//TODO:implement
		return null;
	}
	
	private InProceedings makeInProceedingsObject(Document doc) {
		//TODO:implement
		return null;
	}
	
	private Person makePersonObject(Document doc) {
		//TODO:implement
		return null;
	}
	
	private Proceedings makeProceedingsObject(Document doc) {
		
		Proceedings proceedings = new Proceedings((String)doc.get(Config.MONGODB_PRIMARY_KEY));
		
		String title = (String)doc.get(Config.PROCEEDINGS_TITLE);
		int year = (int)doc.get(Config.PROCEEDINGS_YEAR);
		String isbn = (String)doc.get(Config.PROCEEDINGS_ISBN);
		String volume = (String)doc.get(Config.PROCEEDINGS_VOLUME);
		String note = (String)doc.get(Config.PROCEEDINGS_NOTE);
		String ee = (String)doc.get(Config.PROCEEDINGS_ELECTRONIC_EDITION);
		int number = (int)doc.get(Config.PROCEEDINGS_NUMBER);
		
		proceedings.setTitle(title);
		proceedings.setYear(year);
		proceedings.setIsbn(isbn);
		proceedings.setVolume(volume);
		proceedings.setNote(note);
		proceedings.setElectronicEdition(ee);
		proceedings.setNumber(number);
		
		String series_key = (String)doc.get(Config.PROCEEDINGS_SERIES_KEY);
		Series series = getSeriesById(series_key);
		proceedings.setSeries(series);
		
		
		String conf_ed_key = (String)doc.get(Config.PROCEEDINGS_CONFERENCE_EDITION_KEY);
		ConferenceEdition conferenceEdition = getConferenceEditionById(conf_ed_key);
		proceedings.setConferenceEdition(conferenceEdition);
		
		
		String publisher_key = (String)doc.get(Config.PROCEEDINGS_PUBLISHER_KEY);
		Publisher publisher = getPublisherById(publisher_key);
		proceedings.setPublisher(publisher);
		
		
		ArrayList<String> editor_keys = (ArrayList<String>)doc.get(Config.PROCEEDINGS_EDITOR_KEYS);
		ArrayList<Person> authors = new ArrayList<>();
		for(String key : editor_keys){
			Person p = getPersonById(key);
			authors.add(p);
		}
		proceedings.setAuthors(authors);
		
		
		ArrayList<String> inproceedings_keys = (ArrayList<String>)doc.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
		HashSet<InProceedings> inproceedingsList = new HashSet<>();
		for(String key : inproceedings_keys){
			InProceedings inProceedings = getInProceedingsById(key);
			inproceedingsList.add(inProceedings);
		}
		proceedings.setPublications(inproceedingsList);
		
		return proceedings;
	}
	
	private Publisher makePublisherObject(Document doc) {
		//TODO:implement
		return null;
	}
	
	private Series makeSeriesObject(Document doc) {
		//TODO:implement
		return null;
	}
	
	
	//George: How to query and return domain classes
	public Proceedings getProceedingsById(String id){
		
		
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		Document doc = collection.find(eq("_id", id)).first();
		
		if(doc != null){
			return makeProceedingsObject(doc);
			
		}else{
			
			return null;
			
		}

	}
	
	//George: Inserts authors/editors
	//Note: to get authoredPublication, get all inProceedings where this person is author
	//Note: to get editedPublications, get all proceedings where this person is editor/author
	public void insertPeople(HashMap<Integer, Person> people){
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PEOPLE_COLLECTION);
		
		for (Integer key : people.keySet()){
			Person person = people.get(key);
			Document doc = new Document("_id", key.toString())
							.append(Config.PEOPLE_NAME, person.getName());
			
			documents.add(doc);

		}
		
		
		collection.insertMany(documents);
	}
	
	//George: Inserts conferences
	public void insertConferences(HashMap<String, Conference> conferences){
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
		
		for (String key : conferences.keySet()){
			Conference conf = conferences.get(key);
			Document doc = new Document("_id", key)
							.append(Config.CONFERENCE_NAME, conf.getName());
			
			//add Conference editions
			ArrayList<String> edition_keys = new ArrayList<>();
			for(ConferenceEdition edition : conf.getEditions()){
				edition_keys.add(edition.getId());
			}
			
			doc.append(Config.CONFERENCE_EDITION_KEYS, edition_keys);
			documents.add(doc);
		}
		collection.insertMany(documents);
	}
	
	//George: Inserts conferenceEditions
	public void insertConferenceEditions(HashMap<String, ConferenceEdition> conferenceEditions){
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
		
		for (String key : conferenceEditions.keySet()){
			ConferenceEdition confEd = conferenceEditions.get(key);
			Document doc = new Document("_id", key)
							.append(Config.CONFERENCE_EDITION_YEAR, confEd.getYear())
							.append(Config.CONFERENCE_EDITION_PROCEEDINGS_KEY, confEd.getProceedings().getId());
			
			documents.add(doc);
		}
		collection.insertMany(documents);
	}
	
	//George: Inserts Publishers
	//Note: to GetPublications of a publisher: 
	//get all proceedings for a publisher and all inproceedings in a proceedings
	public void insertPublishers(HashMap<String, Publisher> publishers){
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PUBLISHER_COLLECTION);
		for(String key : publishers.keySet()){
			Publisher publisher = publishers.get(key);
			Document doc = new Document("_id", key)
					.append(Config.PUBLISHER_NAME, publisher.getName());
			documents.add(doc);
			
		}
		collection.insertMany(documents);
	}
	
	//George: Inserts Series
	//Note: to GetPublications of a series: 
	//get all proceedings for series and all inproceedings in a proceedings
	public void insertSeries(HashMap<Integer, Series> seriesList){
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.SERIES_COLLECTION);
		for(Integer key : seriesList.keySet()){
			Series series = seriesList.get(key);
			Document doc = new Document("_id", key.toString())
					.append(Config.SERIES_NAME, series.getName());
			
			
			documents.add(doc);
			
		}
		collection.insertMany(documents);
	}

	//George: Inserts Proceedings
	public void insertProceedings(HashMap<String, Proceedings> proceedingsList ){
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		for(String key : proceedingsList.keySet()){
			Proceedings proceedings = proceedingsList.get(key);
			Document doc = new Document("_id", key)
					.append(Config.PROCEEDINGS_TITLE, proceedings.getTitle())
					.append(Config.PROCEEDINGS_YEAR, proceedings.getYear())
					.append(Config.PROCEEDINGS_ISBN, proceedings.getIsbn())
					.append(Config.PROCEEDINGS_VOLUME, proceedings.getVolume())
					.append(Config.PROCEEDINGS_NOTE, proceedings.getNote())
					.append(Config.PROCEEDINGS_ELECTRONIC_EDITION, proceedings.getElectronicEdition())
					.append(Config.PROCEEDINGS_NUMBER, proceedings.getNumber());
			
			//could be null
			if(proceedings.getSeries() != null){
				doc.append(Config.PROCEEDINGS_SERIES_KEY, proceedings.getSeries().getId());
			}
			
			//could be null
			if(proceedings.getConferenceEdition() != null){
				doc.append(Config.PROCEEDINGS_CONFERENCE_EDITION_KEY, proceedings.getConferenceEdition().getId());
			}
			
			//could be null
			if(proceedings.getPublisher() != null){
				doc.append(Config.PROCEEDINGS_PUBLISHER_KEY, proceedings.getPublisher().getId());
			}
			
			//add editors
			ArrayList<String> editor_keys = new ArrayList<>();
			for(Person editor : proceedings.getAuthors()){
				editor_keys.add(editor.getId());
			}
			
			//add inproceedings
			ArrayList<String> inProceeding_keys = new ArrayList<>();
			for(InProceedings inProceeding : proceedings.getPublications()){
				inProceeding_keys.add(inProceeding.getId());
			}
			
			doc.append(Config.PROCEEDINGS_EDITOR_KEYS, editor_keys);
			doc.append(Config.PROCEEDINGS_INPROCEEDING_KEYS, inProceeding_keys);
			documents.add(doc);
			
		}
		collection.insertMany(documents);
	}
	
	//George: Inserts inProceedings
	public void insertInProceedings(HashMap<String, InProceedings> inProceedingsList ){
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		
		for(String key : inProceedingsList.keySet()){
			InProceedings inProceedings = inProceedingsList.get(key);
			Document doc = new Document("_id", key)
					.append(Config.INPROCEEDINGS_TITLE, inProceedings.getTitle())
					.append(Config.INPROCEEDINGS_YEAR, inProceedings.getYear())
					.append(Config.INPROCEEDINGS_NOTE, inProceedings.getNote())
					.append(Config.INPROCEEDINGS_ELECTRONIC_EDITION, inProceedings.getElectronicEdition())
					.append(Config.INPROCEEDINGS_PAGES, inProceedings.getPages())
					;
			
			//could be null
			if(inProceedings.getProceedings() != null){
				doc.append(Config.INPROCEEDINGS_PROCEEDINGS_KEY, inProceedings.getProceedings().getId());
			}
			
			
			//add authors
			ArrayList<String> author_keys = new ArrayList<>();
			for(Person author : inProceedings.getAuthors()){
				author_keys.add(author.getId());
			}
			doc.append(Config.INPROCEEDINGS_AUTHOR_KEYS, author_keys);
			documents.add(doc);
		}
		
		collection.insertMany(documents);
		
	}
	
	public void removeInProceedings(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}
	
	public void removeProceedings(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}
	
	public void removePerson(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PEOPLE_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}
	
	public void removeConference(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}
	
	public void removeConferenceEdition(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}
	
	public void removePublisher(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PUBLISHER_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}
	
	public void removeSeries(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.SERIES_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}
	
	
	public String getName(){
		return dbName;
	}
	
	
	//George: commits the imported XML data to ZooDB
	public void importData(HashMap<String, Proceedings> proceedingsList, HashMap<String, InProceedings> inProceedingsList,
							HashMap<Integer, Series> seriesList,
							HashMap<String, Publisher> publishers,
							HashMap<String, ConferenceEdition> conferenceEditions,
							HashMap<String, Conference> conferences,
							HashMap<Integer, Person> people){

		insertPeople(people);
		insertConferences(conferences);
		insertConferenceEditions(conferenceEditions);
		insertPublishers(publishers);
		insertSeries(seriesList);
		insertProceedings(proceedingsList );
		insertInProceedings(inProceedingsList );
	}
	
	
	
	

	
}
