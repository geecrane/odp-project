package ch.ethz.globis.mtfobu.odb_project;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;





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
	
	//George: How to query and return domain classes
	public Proceedings getProceedingsById(String id){
		Proceedings proceedings = new Proceedings(id);
		
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		Document doc = collection.find(eq("_id", id)).first();
		
		if(doc != null){
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
